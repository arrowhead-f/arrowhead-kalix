package se.arkalix.core.plugin.dto;

import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.json.JsonName;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * A service definition, as it appears in a {@link ServiceQueryResult}.
 */
@DtoReadableAs(JSON)
public interface ServiceDefinition {
    /**
     * Service name.
     */
    @JsonName("serviceDefinition")
    ServiceName name();

    /**
     * Service provider.
     */
    SystemDefinition provider();

    /**
     * Service qualifier, or URI.
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
     * @see se.arkalix.descriptor.InterfaceDescriptor InterfaceDescriptor
     */
    List<InterfaceName> interfaces();
}
