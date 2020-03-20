package se.arkalix.internal.plugin;

import se.arkalix.ArService;
import se.arkalix.ArSystem;
import se.arkalix.description.ServiceDescription;
import se.arkalix.plugin.Plug;
import se.arkalix.plugin.Plugin;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Futures;
import se.arkalix.util.function.ThrowingBiConsumer;
import se.arkalix.util.function.ThrowingBiFunction;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Internal
public class PluginNotifier {
    private final Map<Plug, Plugin> plugins;

    public PluginNotifier(final ArSystem system, final Collection<Plugin> plugins0) {
        this.plugins = plugins0 == null
            ? Collections.emptyMap()
            : plugins0.stream().collect(Collectors.toConcurrentMap(plugin ->
                new Plug() {
                    @Override
                    public void detach() {
                        final var plugin = plugins.remove(this);
                        if (plugin != null) {
                            try {
                                plugin.onDetach(this);
                            }
                            catch (final Throwable throwable) {
                                throwable.printStackTrace(); // TODO: Log properly.
                            }
                        }
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

    public void clear() {
        forEach((plug, plugin) -> plugin.onDetach(plug));
        plugins.clear();
    }

    public Future<?> onServicePrepared(final ArService service) {
        return serialize((plug, plugin) -> plugin.onServicePrepared(plug, service));
    }

    public Future<?> onServiceProvided(final ServiceDescription service) {
        return serialize((plug, plugin) -> plugin.onServiceProvided(plug, service));
    }

    public void onServiceDismissed(final ServiceDescription service) {
        forEach((plug, plugin) -> plugin.onServiceDismissed(plug, service));
    }

    private void forEach(final ThrowingBiConsumer<Plug, Plugin> consumer) {
        for (final var entry : plugins.entrySet()) {
            final var plugin = entry.getValue();
            final var plug = entry.getKey();
            try {
                consumer.accept(plug, plugin);
            }
            catch (final Throwable throwable) {
                plugins.remove(plug);
                try {
                    plugin.onDetach(plug, throwable);
                }
                catch (final Throwable throwable1) {
                    throwable1.addSuppressed(throwable);
                    throwable1.printStackTrace(); // TODO: Log properly.
                }
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
}
