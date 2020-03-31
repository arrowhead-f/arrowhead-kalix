package se.arkalix.core.plugin.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.json.JsonName;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * The name of a service.
 */
@DtoReadableAs(JSON)
public interface ServiceName {
    /**
     * Service name.
     */
    @JsonName("serviceDefinition")
    String name();
}
