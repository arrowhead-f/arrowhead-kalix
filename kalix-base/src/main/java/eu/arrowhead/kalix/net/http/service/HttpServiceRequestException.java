package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.net.http.HttpStatus;

/**
 * Signifies that some HTTP request received by a {@link HttpService} contains
 * an error and, therefore, will not be processed.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces</i>. If an HTTP request causes an error that should generate a stack
 * trace, some other exception should be used instead.
 */
public class HttpServiceRequestException extends Exception {
    private final HttpStatus status;

    /**
     * Creates new HTTP request exception with given HTTP status and message.
     *
     * @param status  HTTP status.
     * @param message Human-readable description of issue.
     */
    public HttpServiceRequestException(final HttpStatus status, final String message) {
        super(message, null, false, false); // Disable stack trace.
        this.status = status;
    }

    /**
     * @return HTTP status code to use in response to requester.
     */
    public HttpStatus status() {
        return status;
    }

    @Override
    public String getMessage() {
        return status + " " + super.getMessage();
    }
}
