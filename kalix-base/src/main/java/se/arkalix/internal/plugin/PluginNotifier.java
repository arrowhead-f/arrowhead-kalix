package se.arkalix.internal.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArService;
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
import java.util.stream.Collectors;

@Internal
public class PluginNotifier {
    private static final Logger logger = LoggerFactory.getLogger(PluginNotifier.class);

    private final Map<Plug, Plugin> plugins;

    public PluginNotifier(final ArSystem system, final Collection<Plugin> plugins0) {
        this.plugins = plugins0 == null
            ? Collections.emptyMap()
            : plugins0.stream().collect(Collectors.toConcurrentMap(plugin -> new Plug() {
                @Override
                public void detach() {
                    PluginNotifier.detach(this, plugins, null);
                }

                @Override
                public ArSystem system() {
                    return system;
                }

                @Override
                public boolean isSystemShuttingDown() {
                    return system.isShuttingDown();
                }
            },
            plugin -> plugin)
        );
        forEach((plug, plugin) -> plugin.onAttach(plug));
    }

    @ThreadSafe
    public void clear() {
        forEach((plug, plugin) -> plugin.onDetach(plug));
        plugins.clear();
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
            .serialize(plugins.entrySet().stream().map(entry -> {
                final var plug = entry.getKey();
                final var plugin = entry.getValue();
                try {
                    return plugin.onServiceQueried(plug, query);
                }
                catch (final Throwable throwable) {
                    return Future.failure(throwable);
                }
            }))
            .map(collections -> collections.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet()));
    }

    private void forEach(final ThrowingBiConsumer<Plug, Plugin> consumer) {
        for (final var entry : plugins.entrySet()) {
            final var plugin = entry.getValue();
            final var plug = entry.getKey();
            try {
                consumer.accept(plug, plugin);
            }
            catch (final Throwable throwable) {
                detach(plug, plugins, throwable);
            }
        }
    }

    private Future<?> serialize(final ThrowingBiFunction<Plug, Plugin, Future<?>> function) {
        return Futures.serialize(plugins.entrySet()
            .stream()
            .map(entry -> {
                try {
                    return function.apply(entry.getKey(), entry.getValue());
                }
                catch (final Throwable throwable) {
                    return Future.failure(throwable);
                }
            }));
    }

    private static void detach(final Plug plug, final Map<Plug, Plugin> plugins, final Throwable throwable0) {
        final var plugin = plugins.remove(plug);
        if (plugin == null) {
            return;
        }
        if (throwable0 == null) {
            try {
                plugin.onDetach(plug);
            }
            catch (final Throwable throwable1) {
                try {
                    plugin.onDetach(plug, throwable1);
                }
                catch (final Throwable throwable2) {
                    if (logger.isErrorEnabled()) {
                        throwable1.addSuppressed(throwable1);
                        logger.error("Failed to detach plugin \"" + plugin + "\"", throwable2);
                    }
                }
            }
        }
        else {
            try {
                plugin.onDetach(plug, throwable0);
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
    }
}
