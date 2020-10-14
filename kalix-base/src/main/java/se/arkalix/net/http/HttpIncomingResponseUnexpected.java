package se.arkalix.net.http;

import se.arkalix.net.http.client.HttpClientConnection;

/**
 * Signifies that some HTTP response received via a
 * {@link HttpClientConnection} was rejected due to not satisfying some
 * arbitrary requirement.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces</i>. If an HTTP response causes an error that should generate a stack
 * trace, some other exception type should be used instead.
 */
public class HttpIncomingResponseUnexpected extends HttpOutgoingRequestException {
    private final HttpIncomingResponse response;

    /**
     * Creates new HTTP response rejection exception from given response.
     *
     * @param response Response not containing the expected result.
     */
    public HttpIncomingResponseUnexpected(final HttpIncomingResponse response) {
        this(response, null, null);
    }

    /**
     * Creates new HTTP response rejection exception from given response and
     * message.
     *
     * @param response Response not containing the expected result.
     * @param message  Human-readable description of issue.
     */
    public HttpIncomingResponseUnexpected(final HttpIncomingResponse response, final String message) {
        this(response, message, null);
    }

    /**
     * Creates new HTTP response rejection exception from given response and
     * message.
     *
     * @param response Response not containing the expected result.
     * @param message  Human-readable description of issue.
     * @param cause    Exception thrown due to response not containing expected
     *                 result.
     */
    public HttpIncomingResponseUnexpected(
        final HttpIncomingResponse response,
        final String message,
        final Throwable cause
    ) {
        super(response, response.request(), message, cause);
        this.response = response;
    }

    /**
     * @return HTTP status contained in offending response.
     */
    public HttpStatus status() {
        return response.status();
    }

    /**
     * @return HTTP headers contained in offending response.
     */
    public HttpHeaders headers() {
        return response.headers();
    }

    /**
     * @return Offending response.
     */
    public HttpIncomingResponse response() {
        return response;
    }

    @Override
    protected String format() {
        return super.format() + " -> [" + response.status().text() + "]";
    }
}
