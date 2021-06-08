package se.arkalix._internal;

import se.arkalix.ArSystem;
import se.arkalix.plugin._internal.PluginNotifier;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

@FunctionalInterface
@Internal
public interface ArServerFactory {
    Future<ArServer> create(ArSystem system, PluginNotifier pluginNotifier);
}
