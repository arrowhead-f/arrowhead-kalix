package se.arkalix.internal;

import se.arkalix.ArServiceDescriptionCache;
import se.arkalix.description.ServiceDescription;
import se.arkalix.util.annotation.Internal;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

@Internal
public class DefaultServiceDescriptionCache implements ArServiceDescriptionCache {
    private final ConcurrentSkipListSet<ServiceDescription> services = new ConcurrentSkipListSet<>();
    private final Duration entryLifetimeLimit;

    public DefaultServiceDescriptionCache(final Duration entryLifetimeLimit) {
        this.entryLifetimeLimit = Objects.requireNonNull(entryLifetimeLimit, "Expected entryLifetimeLimit");
    }

    @Override
    public void clear() {
        services.clear();
    }

    @Override
    public Stream<ServiceDescription> getByName(final String name) {
        return getAll().filter(service -> service.name().equals(name));
    }

    @Override
    public Stream<ServiceDescription> getAll() {
        final var now = Instant.now();
        services.removeIf(service -> now.isAfter(service.expiresAt()) ||
            now.isAfter(service.receivedAt().plus(entryLifetimeLimit)));
        return services.stream();
    }

    @Override
    public void update(Stream<ServiceDescription> services) {
        services.forEach(service -> {
            if (!this.services.add(service)) {
                this.services.remove(service);
                this.services.add(service);
            }
        });
    }
}
