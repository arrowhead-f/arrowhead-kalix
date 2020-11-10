package se.arkalix.core.plugin;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.json.DtoJsonName;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * The name of a service.
 */
@DtoReadableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface ServiceName {
    /**
     * Service name.
     */
    @DtoJsonName("serviceDefinition")
    String name();
}
