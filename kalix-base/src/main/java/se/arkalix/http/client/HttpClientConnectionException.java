package se.arkalix.http.client;

/**
 * Signifies that some HTTP client failed to establish some
 * {@link HttpClientConnection}.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces</i>. If an HTTP response causes an error that should generate a stack
 * trace, some other exception type should be used instead.
 */
public class HttpClientConnectionException extends HttpClientException {
    /**
     * Creates new HTTP client connection exception with given message.
     *
     * @param message Human-readable description of issue.
     */
    public HttpClientConnectionException(final String message) {
        super(message);
    }
}
