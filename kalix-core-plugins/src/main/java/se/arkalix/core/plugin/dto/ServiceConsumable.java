package se.arkalix.core.plugin.dto;

import se.arkalix.descriptor.InterfaceDescriptor;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.json.JsonName;

import java.util.List;
import java.util.Map;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * Details about some {@link ServiceDetails service} with additional
 * information related to how it can be accessed.
 */
@DtoReadableAs(JSON)
public interface ServiceConsumable extends ServiceDetails {
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
}
