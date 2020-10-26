package se.arkalix;

import se.arkalix._internal.DefaultServiceDescriptionCache;
import se.arkalix.util.annotation.ThreadSafe;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * A local service registry, maintaining descriptions of known remote services
 * whose service registry entries have not yet expired.
 */
@SuppressWarnings("unused")
public interface ArServiceDescriptionCache {
    /**
     * Creates a new service cache that will not hold on to provided service
     * descriptions for longer than a default duration.
     *
     * @return New service cache.
     */
    static ArServiceDescriptionCache withDefaultEntryLifetimeLimit() {
        return new DefaultServiceDescriptionCache(Duration.ofMinutes(5));
    }

    /**
     * Creates a new service cache that will not hold on to provided service
     * descriptions for longer than specified by the given
     * {@code entryLifetimeLimit}.
     *
     * @param entryLifetimeLimit Duration after which service descriptions
     *                           added to this cache become stale and are
     *                           removed, even if they explicitly specify a
     *                           later {@link ServiceRecord#expiresAt()
     *                           expiration time}.
     * @return New service cache.
     */
    static ArServiceDescriptionCache withEntryLifetimeLimit(final Duration entryLifetimeLimit) {
        return new DefaultServiceDescriptionCache(entryLifetimeLimit);
    }

    /**
     * Empties cache.
     */
    @ThreadSafe
    void clear();

    /**
     * Gets stream of all services in cache matching given {@code name}.
     *
     * @param name Name to search for.
     * @return Stream of matching service descriptions.
     */
    @ThreadSafe
    Stream<ServiceRecord> getByName(final String name);

    /**
     * Gets stream of all services in cache matching given {@code name} and
     * supporting at least one of the specified {@code interfaces}.
     *
     * @param name       Name to search for.
     * @param interfaces Interfaces to search for.
     * @return Stream of matching service descriptions.
     */
    @ThreadSafe
    default Stream<ServiceRecord> getByNameAndInterfaces(
        final String name,
        final ServiceInterface... interfaces
    ) {
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
    default Stream<ServiceRecord> getByNameAndInterfaces(
        final String name,
        final List<ServiceInterface> interfaces
    ) {
        return getAll().filter(service -> service.name().equals(name) &&
            interfaces.stream().anyMatch(interfaces::contains));
    }

    /**
     * Returns ordered stream of all services in cache.
     *
     * @return Stream of all services in cache.
     */
    @ThreadSafe
    Stream<ServiceRecord> getAll();

    /**
     * Adds or replaces existing service entries in cache.
     *
     * @param services Services to update.
     */
    @ThreadSafe
    default void update(final ServiceRecord... services) {
        update(Stream.of(services));
    }

    /**
     * Adds or replaces existing service entries in cache.
     *
     * @param services Services to update.
     */
    @ThreadSafe
    default void update(final Collection<ServiceRecord> services) {
        update(services.stream());
    }

    /**
     * Adds or replaces existing service entries in cache.
     *
     * @param services Services to update.
     */
    @ThreadSafe
    void update(Stream<ServiceRecord> services);
}
