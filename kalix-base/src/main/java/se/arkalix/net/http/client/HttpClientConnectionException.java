package se.arkalix.net.http.client;

/**
 * Signifies that some {@link HttpClient} failed to establish a
 * {@link HttpClientConnection}.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces</i>. If an HTTP response causes an error that should generate a stack
 * trace, some other exception type should be used instead.
 */
public class HttpClientConnectionException extends HttpClientException {
    /**
     * Creates new {@link HttpClient} connection exception with given message.
     *
     * @param message Human-readable description of cause of exception being
     *                thrown.
     */
    public HttpClientConnectionException(final String message) {
        super(message);
    }
}
