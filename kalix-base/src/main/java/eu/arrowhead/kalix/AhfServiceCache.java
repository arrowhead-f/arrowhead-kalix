package eu.arrowhead.kalix;

import eu.arrowhead.kalix.description.ServiceDescription;
import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

/**
 * A local service registry, maintaining descriptions of known remote services.
 */
public class AhfServiceCache {
    private final ConcurrentSkipListSet<ServiceDescription> services =
        new ConcurrentSkipListSet<>(new ServiceDescriptionComparator());

    /**
     * Gets stream of all services in cache matching given {@code name}.
     *
     * @param name Name to search for.
     * @return Stream of matching service descriptions.
     */
    public Stream<ServiceDescription> getByName(final String name) {
        return services.stream().filter(service -> service.name().equals(name));
    }

    /**
     * Gets stream of all services in cache matching given {@code name} and
     * supporting at least one of the specified {@code interfaces}.
     *
     * @param name       Name to search for.
     * @param interfaces Interfaces to search for.
     * @return Stream of matching service descriptions.
     */
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
    public Stream<ServiceDescription> getByNameAndInterfaces(
        final String name,
        final List<InterfaceDescriptor> interfaces)
    {
        return services.stream().filter(service -> service.name().equals(name) &&
            interfaces.stream().anyMatch(interfaces::contains));
    }

    /**
     * Returns ordered stream of all services in cache.
     *
     * @return Stream of all services in cache.
     */
    public Stream<ServiceDescription> getAll() {
        return services.stream();
    }

    /**
     * Adds or replaces existing service entries in cache.
     *
     * @param services Services to update.
     */
    public void update(final ServiceDescription... services) {
        update(Stream.of(services));
    }

    /**
     * Adds or replaces existing service entries in cache.
     *
     * @param services Services to update.
     */
    public void update(final Collection<ServiceDescription> services) {
        update(services.stream());
    }

    /**
     * Adds or replaces existing service entries in cache.
     *
     * @param services Services to update.
     */
    public void update(Stream<ServiceDescription> services) {
        services.forEach(service -> {
            if (!this.services.add(service)) {
                this.services.remove(service);
                this.services.add(service);
            }
        });
    }

    private static final class ServiceDescriptionComparator implements Comparator<ServiceDescription> {
        @Override
        public int compare(final ServiceDescription a, final ServiceDescription b) {
            int d;
            d = a.name().compareTo(b.name());
            if (d != 0) {
                return d;
            }
            d = a.qualifier().compareTo(b.qualifier());
            if (d != 0) {
                return d;
            }
            final var aInterfaces = a.supportedInterfaces();
            final var bInterfaces = b.supportedInterfaces();
            if (aInterfaces.size() == 1 && bInterfaces.size() == 1) {
                return aInterfaces.get(0).compareTo(bInterfaces.get(0));
            }
            final var aInterfaceArray = aInterfaces.toArray(new InterfaceDescriptor[0]);
            final var bInterfaceArray = bInterfaces.toArray(new InterfaceDescriptor[0]);
            Arrays.sort(aInterfaceArray);
            Arrays.sort(bInterfaceArray);
            final var i1 = Math.min(aInterfaceArray.length, bInterfaceArray.length);
            for (var i0 = 0; i0 < i1; ++i0) {
                d = aInterfaceArray[i0].compareTo(bInterfaceArray[i0]);
                if (d != 0) {
                    return d;
                }
            }
            return aInterfaceArray.length - bInterfaceArray.length;
        }
    }
}
