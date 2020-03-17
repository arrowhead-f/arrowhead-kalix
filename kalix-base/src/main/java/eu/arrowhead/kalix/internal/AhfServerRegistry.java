package eu.arrowhead.kalix.internal;

import eu.arrowhead.kalix.AhfService;
import eu.arrowhead.kalix.AhfSystem;
import eu.arrowhead.kalix.internal.plugin.PluginNotifier;
import eu.arrowhead.kalix.util.annotation.Internal;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Internal
public class AhfServerRegistry {
    private static final Map<Class<? extends AhfService>, Constructor> registry = new ConcurrentHashMap<>();

    private AhfServerRegistry() {}

    public static Optional<Constructor> get(final Class<? extends AhfService> class_) {
        return Optional.ofNullable(registry.get(class_));
    }

    public static void set(final Class<?extends AhfService> class_, final Constructor constructor) {
        registry.put(class_, constructor);
    }

    @FunctionalInterface
    public interface Constructor {
        Future<AhfServer> construct(AhfSystem system, PluginNotifier pluginNotifier);
    }
}
