package se.arkalix.core.plugin.or;

import se.arkalix.core.plugin.ServiceConsumable;
import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.json.DtoJsonName;

import java.util.List;

import static se.arkalix.dto.DtoCodec.JSON;

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
    @DtoJsonName("response")
    List<ServiceConsumable> services();
}
