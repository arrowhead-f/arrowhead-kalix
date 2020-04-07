package se.arkalix.core.plugin.dto;

import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.json.JsonName;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * A service definition, as it appears in a {@link ServiceQueryResult}.
 */
@DtoReadableAs(JSON)
public interface ServiceDetails {
    /**
     * Service name.
     */
    @JsonName("serviceDefinition")
    ServiceName name();

    /**
     * Service provider.
     */
    SystemDetails provider();

    /**
     * Service qualifier, also referred to as service URI.
     * <p>
     * The significance of this value depends on the application-level
     * transport protocol employed by the service. If, for example, HTTP is
     * used, then this is a base path.
     */
    @JsonName("serviceUri")
    String uri();

    /**
     * The date and time at which the service definition entry expires, if
     * ever.
     */
    @JsonName("endOfValidity")
    Optional<String> expiresAt();

    /**
     * The security/authentication mode supported by the service.
     */
    @JsonName("secure")
    SecurityDescriptor security();

    /**
     * Arbitrary service metadata.
     */
    Map<String, String> metadata();

    /**
     * Service version.
     */
    int version();

    /**
     * List of supported network interface triplets.
     *
     * @see se.arkalix.descriptor.InterfaceDescriptor InterfaceDescriptor
     */
    List<InterfaceName> interfaces();

    /**
     * Converts this objects into a {@link ServiceDescription}.
     *
     * @return New {@link ServiceDescription}.
     * @throws RuntimeException If the type of public key held by {@link
     *                          #provider()}, if any, is not supported.
     */
    default ServiceDescription toServiceDescription() {
        return new ServiceDescription.Builder()
            .name(name().name())
            .provider(provider().toProviderDescription())
            .uri(uri())
            .renewAt(expiresAt()
                .map(expiresAt -> {
                    final var instant = Instants.fromAitiaDateTimeString(expiresAt);
                    final var limit = Instant.now().plus(Duration.ofMinutes(10)); // TODO: Make configurable.
                    return limit.isAfter(instant)
                        ? limit
                        : instant.minus(Duration.ofSeconds(10)); // TODO: Make configurable.
                })
                .orElse(null))
            .security(security())
            .metadata(metadata())
            .version(version())
            .interfaces(interfaces()
                .stream()
                .map(InterfaceName::name)
                .collect(Collectors.toUnmodifiableSet()))
            .build();
    }
}
