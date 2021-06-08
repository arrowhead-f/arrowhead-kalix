package se.arkalix.core.plugin;

import se.arkalix.net.http.HttpIncomingResponse;
import se.arkalix.net.http.HttpIncomingResponseUnexpected;

/**
 * Some error caused by a core service responding to a request with an {@link
 * ErrorResponse}.
 */
public class ErrorResponseException extends HttpIncomingResponseUnexpected {
    private final ErrorResponse error;

    /**
     * Creates new HTTP response containing given {@code error}.
     *
     * @param response Offending response.
     * @param error    Core service error to include in exception.
     */
    public ErrorResponseException(final HttpIncomingResponse<?, ?> response, final ErrorResponse error) {
        super(response, error.type() + " [" + error.code() + "]: " + error.message());
        this.error = error;
    }

    /**
     * @return Error causing this exception to be thrown.
     */
    public ErrorResponse error() {
        return error;
    }
}
