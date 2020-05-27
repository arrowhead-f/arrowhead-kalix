package se.arkalix.net.http.client;

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
public class HttpClientResponseException extends HttpClientException {
    private final HttpClientRequest request;

    /**
     * Creates new HTTP response exception with given message.
     *
     * @param request Request that could not be fulfilled due to the condition
     *                described by this exception occurring.
     * @param message Human-readable description of issue.
     */
    public HttpClientResponseException(final HttpClientRequest request, final String message) {
        super("[" + request.method().map(Object::toString).orElse("X") + " " + request.uri().orElse("X") + "] " + message);
        this.request = request;
    }

    /**
     * Gets request associated with this response exception.
     *
     * @return Request that could not be fulfilled due to the condition
     * described by this exception occurring.
     */
    public HttpClientRequest request() {
        return request;
    }
}
