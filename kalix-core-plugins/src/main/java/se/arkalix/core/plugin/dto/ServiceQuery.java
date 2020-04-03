package se.arkalix.core.plugin.dto;

import se.arkalix.descriptor.InterfaceDescriptor;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.JsonName;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * A search query for service definitions.
 */
@DtoWritableAs(JSON)
public interface ServiceQuery {
    /**
     * Service name. <b>Must be specified.</b>
     */
    @JsonName("serviceDefinitionRequirement")
    String name();

    /**
     * Supported service interfaces.
     */
    @JsonName("interfaceRequirements")
    List<InterfaceDescriptor> interfaces();

    /**
     * Permitted security/authentication modes.
     */
    @JsonName("securityRequirements")
    List<SecurityDescriptor> securityModes();

    /**
     * Required metadata pairs.
     */
    @JsonName("metadataRequirements")
    Map<String, String> metadata();

    /**
     * Desired exact service version.
     */
    @JsonName("versionRequirement")
    Optional<Integer> version();

    /**
     * The highest permitted service version.
     */
    @JsonName("maxVersionRequirement")
    Optional<Integer> versionMax();

    /**
     * The lowest permitted service version.
     */
    @JsonName("minVersionRequirement")
    Optional<Integer> versionMin();

    /**
     * Whether or not to test if matching services are live before returning
     * {@link ServiceQueryResult}.
     */
    @JsonName("pingProviders")
    Optional<Boolean> triggerPing();
}
