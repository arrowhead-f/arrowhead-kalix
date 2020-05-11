package se.arkalix.core.plugin.sr;

import se.arkalix.core.plugin.ServiceDetails;
import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.json.JsonName;

import java.util.List;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * The successful result of a {@link ServiceQuery}.
 */
@DtoReadableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface ServiceQueryResult {
    /**
     * Definitions of services matching original {@link ServiceQuery}.
     */
    @JsonName("serviceQueryData")
    List<ServiceDetails> services();
}
