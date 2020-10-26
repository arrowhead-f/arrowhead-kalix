package se.arkalix.core.plugin;

import se.arkalix.core.plugin.or.OrchestrationWarning;
import se.arkalix.ServiceRecord;
import se.arkalix.descriptor.InterfaceDescriptor;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.json.JsonName;
import se.arkalix.core.plugin._internal.Instants;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * Details about some {@link ServiceDetails service} with additional
 * information related to how it can be accessed.
 */
@DtoReadableAs(JSON)
@DtoEqualsHashCode
@DtoToString
@SuppressWarnings("unused")
public interface ServiceConsumable {
    /**
     * Service name.
     */
    @JsonName("service")
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
     * Authorization tokens useful for consuming this service.
     * <p>
     * One token is provided for each {@link InterfaceDescriptor interface
     * triplet} supported by the service.
     */
    @JsonName("authorizationTokens")
    Map<InterfaceDescriptor, String> tokens();

    /**
     * Any notifications about the state of the service that might have bearing
     * on whether or not the service will be consumed.
     */
    List<OrchestrationWarning> warnings();

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
            .interfaceTokens(Stream.concat(
                interfaces().stream().map(i -> Map.entry(i.name(), "")),
                tokens().entrySet().stream())
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue,
                    (a, b) -> a.isBlank() ? b : a)))
            .build();
    }
}
