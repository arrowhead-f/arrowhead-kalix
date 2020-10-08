package se.arkalix.net.http.consumer;

/**
 * Signifies that some {@link HttpConsumer} failed to establish a
 * {@link HttpConsumerConnection}.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces</i>. If an HTTP response causes an error that should generate a stack
 * trace, some other exception type should be used instead.
 */
public class HttpConsumerConnectionException extends RuntimeException {
    /**
     * Creates new HTTP client exception with given message.
     *
     * @param message Human-readable description of issue.
     */
    public HttpConsumerConnectionException(final String message) {
        super(message, null, true, false); // Disable stack trace.
    }
}