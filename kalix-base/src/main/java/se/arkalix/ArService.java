package se.arkalix;

import se.arkalix.codec.CodecType;
import se.arkalix.net.ProtocolType;
import se.arkalix.security.access.AccessPolicy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An Arrowhead Framework service that can be provided by an {@link ArSystem}.
 */
public interface ArService {
    /**
     * Gets service name.
     *
     * @return Service name.
     */
    String name();

    /**
     * Gets service URI.
     *
     * @return Service URI.
     */
    String uri();

    /**
     * Gets identifier for network protocol stack through which this service is
     * made available to other systems.
     *
     * @return Service protocol type.
     */
    ProtocolType protocolType();

    /**
     * Gets access policy enforced by this service.
     *
     * @return Service access policy.
     */
    AccessPolicy accessPolicy();

    /**
     * Gets codecs this service can read and write.
     *
     * @return Unmodifiable list of supported codecs.
     */
    List<CodecType> codecTypes();

    /**
     * Gets service metadata.
     *
     * @return Unmodifiable map of service metadata.
     */
    Map<String, String> metadata();

    /**
     * Gets value from service metadata map associated with given {@code key},
     * if any.
     *
     * @param key Key associated with desired metadata value.
     * @return Value associated with {@code key}, if any.
     */
    default Optional<String> metadata(final String key) {
        return Optional.ofNullable(metadata().get(key));
    }

    /**
     * Gets service version.
     *
     * @return Service version.
     */
    int version();

    /**
     * Creates a new description of this service.
     *
     * @param system System providing this service.
     * @return New description.
     */
    default ServiceRecord describeAsIfProvidedBy(final ArSystem system) {
        final var isSecure = system.isSecure();
        return new ServiceRecord.Builder()
            .name(name())
            .provider(system.description())
            .uri(uri())
            .accessPolicyType(accessPolicy().type())
            .metadata(metadata())
            .version(version())
            .interfaces(codecTypes().stream()
                .map(codec -> ServiceInterface.getOrCreate(protocolType(), isSecure, codec))
                .collect(Collectors.toUnmodifiableList()))
            .build();
    }

}
