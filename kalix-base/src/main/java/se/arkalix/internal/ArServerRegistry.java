package se.arkalix.internal;

import se.arkalix.ArService;
import se.arkalix.ArSystem;
import se.arkalix.internal.plugin.PluginNotifier;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Internal
public class ArServerRegistry {
    private static final Map<Class<? extends ArService>, Constructor> registry = new ConcurrentHashMap<>();

    private ArServerRegistry() {}

    public static Optional<Constructor> get(final Class<? extends ArService> class_) {
        return Optional.ofNullable(registry.get(class_));
    }

    public static void set(final Class<?extends ArService> class_, final Constructor constructor) {
        registry.put(class_, constructor);
    }

    @FunctionalInterface
    public interface Constructor {
        Future<ArServer> construct(ArSystem system, PluginNotifier pluginNotifier);
    }
}
