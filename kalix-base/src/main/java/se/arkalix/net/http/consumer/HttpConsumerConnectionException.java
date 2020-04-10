package se.arkalix.net.http.consumer;

import se.arkalix.net.http.client.HttpClientConnectionException;

/**
 * Signifies that some {@link HttpConsumer} failed to establish a
 * {@link se.arkalix.net.http.consumer.HttpConsumerConnection}.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces</i>. If an HTTP response causes an error that should generate a stack
 * trace, some other exception type should be used instead.
 */
public class HttpConsumerConnectionException extends HttpClientConnectionException {
    /**
     * Creates new {@link HttpConsumer} connection exception with given message.
     *
     * @param message Human-readable description of cause of exception being
     *                thrown.
     */
    public HttpConsumerConnectionException(final String message) {
        super(message);
    }
}
