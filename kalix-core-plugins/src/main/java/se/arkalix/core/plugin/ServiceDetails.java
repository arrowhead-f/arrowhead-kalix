package se.arkalix.core.plugin;

import se.arkalix.core.plugin.sr.ServiceQueryResult;
import se.arkalix.ServiceRecord;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.json.JsonName;
import se.arkalix.internal.core.plugin.Instants;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * A service definition, as it appears in a {@link ServiceQueryResult}.
 */
@DtoReadableAs(JSON)
@DtoEqualsHashCode
@DtoToString
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
     * Converts this objects into a {@link ServiceRecord}.
     *
     * @return New {@link ServiceRecord}.
     * @throws RuntimeException If the type of public key held by {@link
     *                          #provider()}, if any, is not supported.
     */
    default ServiceRecord toServiceDescription() {
        final var provider = provider().toSystemDescription();
        if (!provider.isSecure() && security().isSecure()) {
            throw new IllegalStateException("The description of the \"" +
                name().name() + "\" service implies that it is served over " +
                "a secure transport, but its provider \"" + provider.name() +
                "\" does not specify a public key");
        }
        return new ServiceRecord.Builder()
            .name(name().name())
            .provider(provider)
            .uri(uri())
            .expiresAt(expiresAt()
                .map(Instants::fromAitiaDateTimeString)
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

    static ServiceDetailsDto from(final ServiceRecord description) {
        return new ServiceDetailsBuilder()
            .name(new ServiceNameBuilder().name(description.name()).build())
            .provider(SystemDetails.from(description.provider()))
            .uri(description.uri())
            .expiresAt(Instants.toAitiaDateTimeString(description.expiresAt()))
            .security(description.security())
            .metadata(description.metadata())
            .version(description.version())
            .interfaces(description.interfaces()
                .stream()
                .map(descriptor -> new InterfaceNameBuilder().name(descriptor).build())
                .collect(Collectors.toUnmodifiableList()))
            .build();
    }
}
