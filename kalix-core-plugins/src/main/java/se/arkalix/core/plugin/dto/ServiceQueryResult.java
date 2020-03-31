package se.arkalix.core.plugin.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.json.JsonName;

import java.util.List;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * The successful result of a {@link ServiceQuery}.
 */
@DtoReadableAs(JSON)
public interface ServiceQueryResult {
    /**
     * Definitions of services matching original {@link ServiceQuery}.
     */
    @JsonName("serviceQueryData")
    List<ServiceDefinition> services();
}
