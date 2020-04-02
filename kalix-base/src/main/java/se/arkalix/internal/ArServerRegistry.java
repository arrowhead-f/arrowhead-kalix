package se.arkalix.internal;

import se.arkalix.ArService;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.annotation.ThreadSafe;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Internal
public class ArServerRegistry {
    private static final Map<Class<? extends ArService>, ArServerFactory> registry = new ConcurrentHashMap<>();

    private ArServerRegistry() {}

    @ThreadSafe
    public static Optional<ArServerFactory> get(final Class<? extends ArService> class_) {
        return Optional.ofNullable(registry.get(class_));
    }

    @ThreadSafe
    public static void set(final Class<?extends ArService> class_, final ArServerFactory constructor) {
        registry.put(class_, constructor);
    }
}
