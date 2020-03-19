package eu.arrowhead.kalix.net.http.client;

import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpStatus;

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
public class HttpClientResponseRejectedException extends HttpClientResponseException {
    private final HttpClientResponse response;

    /**
     * Creates new HTTP response rejection exception from given response.
     *
     * @param response Human-readable description of issue.
     */
    public HttpClientResponseRejectedException(final HttpClientResponse response) {
        this(response, response.status().text());
    }

    /**
     * Creates new HTTP response rejection exception from given response and
     * message.
     *
     * @param message Human-readable description of issue.
     */
    public HttpClientResponseRejectedException(final HttpClientResponse response, final String message) {
        super(message);
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
    public HttpClientResponse unwrap() {
        return response;
    }
}
