package se.arkalix.internal.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArService;
import se.arkalix.plugin.PluginAfter;
import se.arkalix.plugin.PluginFirst;
import se.arkalix.query.ServiceQuery;
import se.arkalix.ArSystem;
import se.arkalix.description.ServiceDescription;
import se.arkalix.plugin.Plug;
import se.arkalix.plugin.Plugin;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.annotation.ThreadSafe;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Futures;
import se.arkalix.util.function.ThrowingBiConsumer;
import se.arkalix.util.function.ThrowingBiFunction;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Internal
public class PluginNotifier {
    private static final Logger logger = LoggerFactory.getLogger(PluginNotifier.class);

    private final List<InnerPlug> plugs;

    public PluginNotifier(final ArSystem system, final Collection<Plugin> plugins0) {
        this.plugs = plugins0 == null
            ? Collections.emptyList()
            : plugins0.stream()
            .map(plugin -> new InnerPlug(plugin, system))
            .sorted((a, b) -> {
                final var aClass = a.plugin().getClass();
                final var bClass = b.plugin().getClass();

                final var aFirst = aClass.isAnnotationPresent(PluginFirst.class) ? 1 : 0;
                final var bFirst = bClass.isAnnotationPresent(PluginFirst.class) ? 1 : 0;

                final var dFirst = bFirst - aFirst;
                if (dFirst != 0) {
                    return dFirst;
                }

                final var aAfter = Optional.ofNullable(aClass.getAnnotation(PluginAfter.class))
                    .map(annotation -> Stream.of(annotation.value()).anyMatch(type -> type.isAssignableFrom(bClass)))
                    .orElse(false)
                    ? 1 : 0;

                final var bAfter = Optional.ofNullable(aClass.getAnnotation(PluginAfter.class))
                    .map(annotation -> Stream.of(annotation.value()).anyMatch(type -> type.isAssignableFrom(aClass)))
                    .orElse(false)
                    ? 1 : 0;

                if (aAfter == 1 && bAfter == 1) {
                    throw new IllegalStateException("The plugin \"" + aClass +
                        "\" must be attached after \"" + bClass + "\" " +
                        "according to its @PluginAfter annotation, while " +
                        "\"" + bClass + "\" must be attached after \"" +
                        aClass + "\", as signified by the same annotation; " +
                        "cannot determine plugin attachment order");
                }

                return aAfter - bAfter;
            })
            .collect(Collectors.toUnmodifiableList());
    }

    @ThreadSafe
    public void onAttach() {
        final var plugins = plugs.stream()
            .map(InnerPlug::plugin)
            .collect(Collectors.toUnmodifiableSet());

        forEach((plug, plugin) -> plugin.onAttach(plug, plugins));
    }

    @ThreadSafe
    public void onDetach() {
        forEachReversed((plug, plugin) -> plugin.onDetach(plug));
    }

    @ThreadSafe
    public Future<?> onServicePrepared(final ArService service) {
        return serialize((plug, plugin) -> plugin.onServicePrepared(plug, service));
    }

    @ThreadSafe
    public Future<?> onServiceProvided(final ServiceDescription service) {
        return serialize((plug, plugin) -> plugin.onServiceProvided(plug, service));
    }

    @ThreadSafe
    public void onServiceDismissed(final ServiceDescription service) {
        forEach((plug, plugin) -> plugin.onServiceDismissed(plug, service));
    }

    @ThreadSafe
    public Future<Set<ServiceDescription>> onServiceQueried(final ServiceQuery query) {
        return Futures
            .serialize(plugs.stream().map(plug -> {
                if (plug.isDetached()) {
                    return Future.done();
                }
                try {
                    return plug.plugin().onServiceQueried(plug, query);
                }
                catch (final Throwable throwable) {
                    return Future.failure(throwable);
                }
            }))
            .map(collections -> collections.stream()
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet()));
    }

    private void forEach(final ThrowingBiConsumer<Plug, Plugin> consumer) {
        for (final var plug : plugs) {
            if (plug.isDetached()) {
                continue;
            }
            try {
                consumer.accept(plug, plug.plugin());
            }
            catch (final Throwable throwable) {
                plug.detach(throwable);
            }
        }
    }

    private void forEachReversed(final ThrowingBiConsumer<Plug, Plugin> consumer) {
        for (var i = plugs.size(); i-- != 0; ) {
            final var plug = plugs.get(i);
            if (plug.isDetached()) {
                continue;
            }
            try {
                consumer.accept(plug, plug.plugin());
            }
            catch (final Throwable throwable) {
                plug.detach(throwable);
            }
        }
    }

    private Future<?> serialize(final ThrowingBiFunction<Plug, Plugin, Future<?>> function) {
        return Futures.serialize(plugs.stream()
            .map(plug -> {
                if (plug.isDetached()) {
                    return Future.done();
                }
                try {
                    return function.apply(plug, plug.plugin());
                }
                catch (final Throwable throwable) {
                    return Future.failure(throwable);
                }
            }));
    }

    private class InnerPlug implements Plug {
        private final AtomicBoolean isDetached = new AtomicBoolean(false);
        private final Plugin plugin;
        private final ArSystem system;

        private InnerPlug(final Plugin plugin, final ArSystem system) {
            this.plugin = Objects.requireNonNull(plugin, "Expected plugin");
            this.system = Objects.requireNonNull(system, "Expected system");
        }

        @Override
        public void detach() {
            if (!isDetached.compareAndSet(false, true)) {
                return;
            }
            try {
                plugin.onDetach(this);
            }
            catch (final Throwable throwable0) {
                try {
                    plugin.onDetach(this, throwable0);
                }
                catch (final Throwable throwable1) {
                    if (logger.isErrorEnabled()) {
                        throwable0.addSuppressed(throwable0);
                        logger.error("Failed to detach plugin \"" + plugin + "\"", throwable1);
                    }
                }
            }
        }

        public void detach(final Throwable throwable0) {
            if (!isDetached.compareAndSet(false, true)) {
                return;
            }
            try {
                plugin.onDetach(this, throwable0);
            }
            catch (final Throwable throwable1) {
                if (logger.isErrorEnabled()) {
                    throwable1.addSuppressed(throwable0);
                    logger.error("Failed to detach plugin \"" + plugin +
                            "\"; detach initiated by plugin throwing " +
                            "the suppressed exception",
                        throwable1);

                }
            }
        }

        @Override
        public boolean isDetached() {
            return isDetached.get();
        }

        @Override
        public Plugin plugin() {
            return plugin;
        }

        @Override
        public Collection<? extends Plug> plugs() {
            return plugs;
        }

        @Override
        public ArSystem system() {
            return system;
        }
    }
}
