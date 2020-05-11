package se.arkalix.core.plugin.or;

import se.arkalix.core.plugin.ServiceConsumable;
import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.json.JsonName;

import java.util.List;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * The result of an {@link OrchestrationQuery orchestration query}.
 */
@DtoReadableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface OrchestrationQueryResult {
    /**
     * Services the querying system may consume.
     */
    @JsonName("response")
    List<ServiceConsumable> services();
}
