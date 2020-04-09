package se.arkalix;

import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.InterfaceDescriptor;
import se.arkalix.util.annotation.ThreadSafe;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

/**
 * A local service registry, maintaining descriptions of known remote services
 * whose service registry entries have not yet expired.
 */
public class ArServiceCache {
    private final ConcurrentSkipListSet<ServiceDescription> services = new ConcurrentSkipListSet<>();
    private final Duration entryLifetimeLimit;

    private ArServiceCache(final Duration entryLifetimeLimit) {
        this.entryLifetimeLimit = Objects.requireNonNull(entryLifetimeLimit, "Expected entryLifetimeLimit");
    }

    /**
     * Creates a new service cache that will not hold on to provided service
     * descriptions for longer than a default duration.
     *
     * @return New service cache.
     */
    public static ArServiceCache withDefaultEntryLifetimeLimit() {
        return new ArServiceCache(Duration.ofMinutes(5));
    }

    /**
     * Creates a new service cache that will not hold on to provided service
     * descriptions for longer than specified by the given
     * {@code entryLifetimeLimit}.
     *
     * @param entryLifetimeLimit Duration after which service descriptions
     *                           added to this cache become stale and are
     *                           removed, even if they explicitly specify a
     *                           later {@link ServiceDescription#expiresAt()
     *                           expiration time}.
     * @return New service cache.
     */
    public static ArServiceCache withEntryLifetimeLimit(final Duration entryLifetimeLimit) {
        return new ArServiceCache(entryLifetimeLimit);
    }

    /**
     * Empties cache.
     */
    @ThreadSafe
    public void clear() {
        services.clear();
    }

    /**
     * Gets stream of all services in cache matching given {@code name}.
     *
     * @param name Name to search for.
     * @return Stream of matching service descriptions.
     */
    @ThreadSafe
    public Stream<ServiceDescription> getByName(final String name) {
        return getAll().filter(service -> service.name().equals(name));
    }

    /**
     * Gets stream of all services in cache matching given {@code name} and
     * supporting at least one of the specified {@code interfaces}.
     *
     * @param name       Name to search for.
     * @param interfaces Interfaces to search for.
     * @return Stream of matching service descriptions.
     */
    @ThreadSafe
    public Stream<ServiceDescription> getByNameAndInterfaces(
        final String name,
        final InterfaceDescriptor... interfaces)
    {
        return getByNameAndInterfaces(name, Arrays.asList(interfaces));
    }

    /**
     * Gets stream of all services in cache matching given {@code name} and
     * supporting at least one of the specified {@code interfaces}.
     *
     * @param name       Name to search for.
     * @param interfaces Interfaces to search for.
     * @return Stream of matching service descriptions.
     */
    @ThreadSafe
    public Stream<ServiceDescription> getByNameAndInterfaces(
        final String name,
        final List<InterfaceDescriptor> interfaces)
    {
        return getAll().filter(service -> service.name().equals(name) &&
            interfaces.stream().anyMatch(interfaces::contains));
    }

    /**
     * Returns ordered stream of all services in cache.
     *
     * @return Stream of all services in cache.
     */
    @ThreadSafe
    public Stream<ServiceDescription> getAll() {
        final var now = Instant.now();
        services.removeIf(service -> service.expiresAt().isAfter(now) ||
            service.receivedAt().plus(entryLifetimeLimit).isAfter(now));
        return services.stream();
    }

    /**
     * Adds or replaces existing service entries in cache.
     *
     * @param services Services to update.
     */
    @ThreadSafe
    public void update(final ServiceDescription... services) {
        update(Stream.of(services));
    }

    /**
     * Adds or replaces existing service entries in cache.
     *
     * @param services Services to update.
     */
    @ThreadSafe
    public void update(final Collection<ServiceDescription> services) {
        update(services.stream());
    }

    /**
     * Adds or replaces existing service entries in cache.
     *
     * @param services Services to update.
     */
    @ThreadSafe
    public void update(Stream<ServiceDescription> services) {
        services.forEach(service -> {
            if (!this.services.add(service)) {
                this.services.remove(service);
                this.services.add(service);
            }
        });
    }
}
