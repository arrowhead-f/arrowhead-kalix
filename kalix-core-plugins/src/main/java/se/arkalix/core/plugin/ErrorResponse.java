package se.arkalix.core.plugin;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.DtoJsonName;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * An error description, as provided by the core services when a request
 * cannot be fulfilled.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface ErrorResponse {
    /**
     * Human-readable description of error.
     *
     * @return Error description.
     */
    @DtoJsonName("errorMessage")
    String message();

    /**
     * Error code.
     *
     * @return Error code.
     */
    @DtoJsonName("errorCode")
    int code();

    /**
     * Error type descriptor.
     *
     * @return Error type descriptor.
     */
    @DtoJsonName("exceptionType")
    String type();
}
