package eu.arrowhead.kalix.net.http.client;

/**
 * Signifies that some HTTP response received via a
 * {@link HttpClientConnection} either describes or contains an error and,
 * therefore, will not be processed.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces</i>. If an HTTP response causes an error that should generate a stack
 * trace, some other exception type should be used instead.
 */
public class HttpClientResponseException extends RuntimeException {
    /**
     * Creates new HTTP request exception with given HTTP status and message.
     *
     * @param message Human-readable description of issue.
     */
    public HttpClientResponseException(final String message) {
        super(message, null, true, false); // Disable stack trace.
    }
}
