package se.arkalix;

import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.TransportDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * A factory useful for creating {@link ArConsumer} instances.
 *
 * @param <C> Type of {@link ArConsumer} created by this factory class.
 * @see se.arkalix.query.ServiceQuery#oneUsing(ArConsumerFactory) ServiceQuery#oneUsing(ArConsumerFactory)
 * @see ArSystem#consume()
 */
public interface ArConsumerFactory<C extends ArConsumer> {
    /**
     * @return Name, if any, that must be matched by services consumed by
     * consumers created by this factory.
     */
    default Optional<String> serviceName() {
        return Optional.empty();
    }

    /**
     * @return Application-level transport protocols out of which at least one
     * must be supported by any services consumed by any consumers created by
     * this factory.
     */
    Collection<TransportDescriptor> serviceTransports();

    /**
     * @return Message payload encodings out of which at least one must be
     * supported by any services consumed by any consumers created by this
     * factory.
     */
    default Collection<EncodingDescriptor> serviceEncodings() {
        return EncodingDescriptor.allWithDtoSupport();
    }

    /**
     * @return Map of metadata pairs that must be matched by any services
     * consumed by any consumers created by this factory.
     */
    default Map<String, String> serviceMetadata() {
        return Collections.emptyMap();
    }

    /**
     * @return Version, if any, that must be matched by any services consumed
     * by any consumers created by this factory.
     */
    default Optional<Integer> serviceVersion() {
        return Optional.empty();
    }

    /**
     * @return Maximum version, if any, that must be satisfied by any services
     * consumed by any consumers created by this factory.
     */
    default Optional<Integer> serviceVersionMax() {
        return Optional.empty();
    }

    /**
     * @return Minimum version, if any, that must be satisfied by any services
     * consumed by any consumers created by this factory.
     */
    default Optional<Integer> serviceVersionMin() {
        return Optional.empty();
    }

    /**
     * Creates new {@link ArConsumer} from given {@code system} and {@code
     * service} description.
     *
     * @param system  System to consume {@code service}.
     * @param service Description of service to be consumed.
     * @return Created {@link ArConsumer} instance.
     */
    default C create(ArSystem system, ServiceDescription service) {
        return create(system, service, serviceEncodings());
    }

    /**
     * Creates new {@link ArConsumer} from given {@code system}, {@code
     * service} description and {@code encodings}.
     *
     * @param system    System to consume {@code service}.
     * @param service   Description of service to be consumed.
     * @param encodings Message payload encodings explicitly designated as
     *                  supported. If {@link #serviceEncodings()} returns a
     *                  non- empty result, these encodings must be a subset of
     *                  the encodings returned by that method.
     * @return Created {@link ArConsumer} instance.
     */
    C create(ArSystem system, ServiceDescription service, Collection<EncodingDescriptor> encodings);
}
