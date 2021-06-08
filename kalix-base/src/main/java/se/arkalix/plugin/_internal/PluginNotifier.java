package se.arkalix.plugin._internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArService;
import se.arkalix.ArSystem;
import se.arkalix.ServiceRecord;
import se.arkalix.plugin.Plugin;
import se.arkalix.plugin.PluginAttached;
import se.arkalix.plugin.PluginFacade;
import se.arkalix.query.ServiceQuery;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.annotation.ThreadSafe;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Futures;
import se.arkalix.util.function.ThrowingFunction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Internal
public class PluginNotifier {
    private static final Logger logger = LoggerFactory.getLogger(PluginNotifier.class);

    private final ArSystem system;
    private final Collection<Plugin> plugins;

    private List<PluginHandler> handlers;

    public PluginNotifier(final ArSystem system, final Collection<Plugin> plugins) {
        this.system = Objects.requireNonNull(system, "system");
        this.plugins = Objects.requireNonNull(plugins, "plugins");
    }

    public Future<Map<Class<? extends Plugin>, PluginFacade>> onAttach() {
        final var handlers = new ArrayList<PluginHandler>();

        this.handlers = Collections.unmodifiableList(handlers);

        return Futures.serialize(plugins.stream()
            .sorted((a, b) -> {
                final var bClass = b.getClass();
                final var aClass = a.getClass();

                final var aLast = a.dependencies().contains(bClass);
                final var bLast = b.dependencies().contains(aClass);

                if (aLast) {
                    if (bLast) {
                        throw new IllegalStateException(aClass + " and " +
                            bClass + " both depend on each other; cannot " +
                            "determine plugin attachment order; cannot " +
                            "attach plugins to system \"" + system.name() +
                            "\"");
                    }
                    return 1;
                }
                else if (bLast) {
                    return -1;
                }

                return a.ordinal() - b.ordinal();
            })
            .map(plugin -> attach(plugin, handlers)))
            .map(ignored -> {
                final var pluginClassToFacade = new HashMap<Class<? extends Plugin>, PluginFacade>();
                handlers.trimToSize();
                for (final var handler : handlers) {
                    handler.attached()
                        .facade()
                        .ifPresent(facade -> {
                            final var existingClass = pluginClassToFacade
                                .putIfAbsent(handler.plugin().getClass(), facade);
                            if (existingClass == null) {
                                return;
                            }
                            throw new IllegalStateException("Plugins " +
                                "providing facades when attached, such as \"" +
                                existingClass + "\", may not be provided" +
                                "to any one system more than once; " +
                                "cannot attach plugins to system \"" +
                                system.name() + "\"");
                        });
                }
                return Collections.unmodifiableMap(pluginClassToFacade);
            });
    }

    private Future<PluginHandler> attach(final Plugin plugin, final List<PluginHandler> handlers) {
        return Futures.serialize(plugin.dependencies()
            .stream()
            .map(dependencyClass -> handlers.stream()
                .filter(handler -> dependencyClass.isAssignableFrom(handler.plugin().getClass()))
                .map(Future::success)
                .findAny()
                .orElseGet(() -> load(dependencyClass, handlers))))
            .flatMap(handlers0 -> {
                final var dependencies = new HashMap<Class<? extends Plugin>, PluginFacade>();
                for (final var handler : handlers0) {
                    handler.attached()
                        .facade()
                        .ifPresent(facade -> dependencies.put(handler.plugin().getClass(), facade));
                }
                return plugin.attachTo(system, dependencies);
            })
            .map(attached -> {
                final var handler = new PluginHandler(attached, plugin);
                handlers.add(handler);
                return handler;
            })
            .mapFault(Throwable.class, throwable -> new IllegalStateException("" +
                "Plugin " + plugin + " threw exception while being attached " +
                "to system \"" + system.name() + "\"", throwable));
    }

    private Future<PluginHandler> load(
        final Class<? extends Plugin> dependencyClass,
        final List<PluginHandler> handlers)
    {
        Exception suppressedException = null;
        Object pluginObject = null;
        try {
            try {
                final var method = dependencyClass.getMethod("instance");
                if ((method.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
                    pluginObject = method.invoke(null);
                }
            }
            catch (final NoSuchMethodException exception) {
                suppressedException = exception;
            }
            try {
                if (pluginObject == null) {
                    pluginObject = dependencyClass.getConstructor().newInstance();
                }
            }
            catch (final NoSuchMethodException exception) {
                if (suppressedException != null) {
                    exception.addSuppressed(suppressedException);
                }
                throw new IllegalStateException(dependencyClass + " does not " +
                    "have a public static instance() method or a public " +
                    "constructor taking no arguments; cannot automatically " +
                    "satisfy plugin dependency of system \"" + system.name()
                    + "\"", exception);
            }
        }
        catch (final IllegalAccessException | InvocationTargetException | InstantiationException exception) {
            if (suppressedException != null) {
                exception.addSuppressed(suppressedException);
            }
            throw new IllegalStateException("Failed to load an instance of  " +
                dependencyClass + "; cannot automatically satisfy plugin " +
                "dependency of system \"" + system.name() + "\"", exception);
        }
        final Plugin plugin;
        try {
            plugin = dependencyClass.cast(pluginObject);
        }
        catch (final ClassCastException exception) {
            throw new IllegalStateException(dependencyClass + " returned an " +
                "unexpected instance type when its instance() method was " +
                "called; cannot automatically satisfy plugin " +
                "dependency", exception);
        }
        return attach(plugin, handlers);
    }

    public void onDetach() {
        for (var i = handlers.size(); i-- != 0; ) {
            final var handler = handlers.get(i);
            try {
                handler.detach();
            }
            catch (final Throwable throwable) {
                handler.detach(throwable);
            }
        }
    }

    @ThreadSafe
    public Future<?> onServicePrepared(final ArService service) {
        return serialize(attached -> attached.onServicePrepared(service));
    }

    @ThreadSafe
    public Future<?> onServiceProvided(final ServiceRecord service) {
        return serialize(attached -> attached.onServiceProvided(service));
    }

    @ThreadSafe
    public void onServiceDismissed(final ServiceRecord service) {
        for (final var handler : handlers) {
            try {
                handler.attached().onServiceDismissed(service);
            }
            catch (final Throwable throwable) {
                handler.detach(throwable);
            }
        }
    }

    @ThreadSafe
    public Future<Set<ServiceRecord>> onServiceQueried(final ServiceQuery query) {
        return Futures
            .serialize(handlers.stream().map(handler -> {
                try {
                    return handler.attached().onServiceQueried(query);
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

    private Future<?> serialize(final ThrowingFunction<PluginAttached, Future<?>> function) {
        return Futures.serialize(handlers.stream()
            .map(handler -> {
                try {
                    return function.apply(handler.attached());
                }
                catch (final Throwable throwable) {
                    return Future.failure(throwable);
                }
            }));
    }

    private static class PluginHandler {
        private final AtomicBoolean isDetached = new AtomicBoolean(false);
        private final PluginAttached attached;
        private final Plugin plugin;

        private PluginHandler(final PluginAttached attached, final Plugin plugin) {
            this.attached = Objects.requireNonNull(attached, "attached");
            this.plugin = Objects.requireNonNull(plugin, "plugin");
        }

        public PluginAttached attached() {
            return attached;
        }

        public Plugin plugin() {
            return plugin;
        }

        public void detach() {
            if (!isDetached.compareAndSet(false, true)) {
                return;
            }
            try {
                attached.onDetach();
            }
            catch (final Throwable throwable0) {
                try {
                    attached.onDetach(throwable0);
                }
                catch (final Throwable throwable1) {
                    if (logger.isErrorEnabled()) {
                        throwable0.addSuppressed(throwable0);
                        logger.error("Failed to detach attached \"" +
                            attached + "\"", throwable1);
                    }
                }
            }
        }

        public void detach(final Throwable throwable0) {
            if (!isDetached.compareAndSet(false, true)) {
                return;
            }
            try {
                attached.onDetach(throwable0);
            }
            catch (final Throwable throwable1) {
                if (logger.isErrorEnabled()) {
                    throwable1.addSuppressed(throwable0);
                    logger.error("Failed to detach attached plugin \"" +
                            plugin + "\"; detach initiated by attached " +
                            "plugin throwing the suppressed exception",
                        throwable1);

                }
            }
        }
    }
}
