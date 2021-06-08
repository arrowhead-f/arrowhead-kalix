package se.arkalix.core.plugin;

import se.arkalix.security.access.AccessPolicyType;
import se.arkalix.core.plugin.sr.ServiceQueryResult;
import se.arkalix.ServiceRecord;
import se.arkalix.ServiceInterface;
import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.json.DtoJsonName;
import se.arkalix.core.plugin._internal.Instants;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * A service definition, as it appears in a {@link ServiceQueryResult}.
 */
@DtoReadableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface ServiceDetails {
    /**
     * Service name.
     *
     * @return Service name.
     */
    @DtoJsonName("serviceDefinition")
    ServiceName name();

    /**
     * Service provider.
     *
     * @return Service provider details.
     */
    SystemDetails provider();

    /**
     * Service qualifier, also referred to as service URI.
     * <p>
     * The significance of this value depends on the network protocol employed
     * by the service. If, for example, HTTP is used, then this is a base path.
     *
     * @return Service URI.
     */
    @DtoJsonName("serviceUri")
    String uri();

    /**
     * The date and time at which the service definition entry expires, if
     * ever.
     *
     * @return Instant at which this service record expires.
     */
    @DtoJsonName("endOfValidity")
    Optional<String> expiresAt();

    /**
     * The security/authentication mode supported by the service.
     *
     * @return Access policy type supported by the service.
     */
    @DtoJsonName("secure")
    AccessPolicyType security();

    /**
     * Arbitrary service metadata.
     *
     * @return Service metadata map.
     */
    Map<String, String> metadata();

    /**
     * Service version.
     *
     * @return Service version.
     */
    int version();

    /**
     * List of supported network interface triplets.
     *
     * @return List of supported network interface triplets.
     * @see ServiceInterface InterfaceDescriptor
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
            .accessPolicyType(security())
            .metadata(metadata())
            .version(version())
            .interfaces(interfaces()
                .stream()
                .map(InterfaceName::name)
                .collect(Collectors.toUnmodifiableSet()))
            .build();
    }

    static ServiceDetailsDto from(final ServiceRecord description) {
        return new ServiceDetailsDto.Builder()
            .name(new ServiceNameDto.Builder().name(description.name()).build())
            .provider(SystemDetails.from(description.provider()))
            .uri(description.uri())
            .expiresAt(Instants.toAitiaDateTimeString(description.expiresAt()))
            .security(description.accessPolicyType())
            .metadata(description.metadata())
            .version(description.version())
            .interfaces(description.interfaces()
                .stream()
                .map(descriptor -> new InterfaceNameDto.Builder().name(descriptor).build())
                .collect(Collectors.toUnmodifiableList()))
            .build();
    }
}
