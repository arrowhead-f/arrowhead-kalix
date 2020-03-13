package eu.arrowhead.kalix.internal.plugin;

import eu.arrowhead.kalix.ArrowheadService;
import eu.arrowhead.kalix.ArrowheadServiceBuilder;
import eu.arrowhead.kalix.ArrowheadSystem;
import eu.arrowhead.kalix.plugin.Plug;
import eu.arrowhead.kalix.plugin.Plugin;
import eu.arrowhead.kalix.util.annotation.Internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Internal
public class PluginNotifier {
    private final Map<Plug, Plugin> plugins;

    public PluginNotifier(final ArrowheadSystem system, final Collection<Plugin> plugins0) {
        this.plugins = plugins0 == null
            ? Collections.emptyMap()
            : plugins0.stream().collect(Collectors.toMap(plugin ->
                new Plug() {
                    @Override
                    public void detach() {
                        plugins.remove(this).onDetach(this);
                    }

                    @Override
                    public ArrowheadSystem system() {
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

    public void onServiceBuilding(final ArrowheadServiceBuilder builder) {
        forEach((plug, plugin) -> plugin.onServicePrepared(plug, builder));
    }

    public void onServiceProvided(final ArrowheadService service) {
        forEach((plug, plugin) -> plugin.onServiceProvided(plug, service));
    }

    public void onServiceDismissed(final ArrowheadService service) {
        forEach((plug, plugin) -> plugin.onServiceDismissed(plug, service));
    }

    public void onSystemStarted() {
        forEach((plug, plugin) -> plugin.onSystemStarted(plug));
    }

    public void onSystemStopped() {
        forEach((plug, plugin) -> plugin.onSystemStopped(plug));
    }

    private void forEach(final BiConsumer<Plug, Plugin> consumer) {
        for (final var entry : plugins.entrySet()) {
            final var plugin = entry.getValue();
            final var plug = entry.getKey();
            try {
                consumer.accept(plug, plugin);
            }
            catch (final Throwable throwable) {
                throwable.printStackTrace(); // TODO: Log properly.
                plugins.remove(plug);
                try {
                    plugin.onDetach(plug, throwable);
                }
                catch (final Throwable throwable1) {
                    throwable1.printStackTrace(); // TODO: Log properly.
                }
            }
        }
    }
}
