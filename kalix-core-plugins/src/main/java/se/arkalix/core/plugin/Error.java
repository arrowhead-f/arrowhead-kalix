package se.arkalix.core.plugin;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.json.JsonName;
import se.arkalix.net.http.client.HttpClientResponseException;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * An error description, as provided by the core services when a request
 * cannot be fulfilled.
 */
@DtoReadableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface Error {
    /**
     * Human-readable description of error.
     */
    @JsonName("errorMessage")
    String message();

    /**
     * Error code.
     */
    @JsonName("errorCode")
    int code();

    /**
     * Error type descriptor.
     */
    @JsonName("exceptionType")
    String type();

    /**
     * @return This error as a {@link HttpClientResponseException}.
     */
    default ErrorException toException() {
        return new ErrorException(this);
    }
}
