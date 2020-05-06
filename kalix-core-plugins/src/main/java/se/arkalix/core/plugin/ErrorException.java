package se.arkalix.core.plugin;

import se.arkalix.net.http.client.HttpClientResponseException;

/**
 * Some error caused by a core service responding to a request with an {@link
 * Error}.
 */
public class ErrorException extends HttpClientResponseException {
    private final Error error;

    /**
     * Creates new HTTP response containing given {@code error}.
     *
     * @param error Core service error to include in exception.
     */
    public ErrorException(final Error error) {
        super(error.type() + " [" + error.code() + "]: " + error.message());
        this.error = error;
    }

    /**
     * @return Error causing this exception to be thrown.
     */
    public Error error() {
        return error;
    }
}
