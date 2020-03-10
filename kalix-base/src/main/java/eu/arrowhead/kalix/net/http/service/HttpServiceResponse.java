package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.net.http.HttpBodySender;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.HttpVersion;

import java.util.Optional;

/**
 * An outgoing HTTP response, to be sent by an {@link HttpService}.
 */
public interface HttpServiceResponse extends HttpBodySender<HttpServiceResponse> {
    /**
     * Removes all currently set headers.
     *
     * @return This response object.
     */
    HttpServiceResponse clearHeaders();

    /**
     * Gets a response header value by name.
     *
     * @param name Name of header. Case insensitive. Should be lower-case.
     * @return Header value, if any.
     */
    default Optional<String> header(final CharSequence name) {
        return headers().get(name);
    }

    /**
     * Sets a response header.
     *
     * @param name  Name of header. Case insensitive. Should be lower-case.
     * @param value Header value.
     * @return This response object.
     */
    default HttpServiceResponse header(final CharSequence name, final CharSequence value) {
        headers().set(name, value);
        return this;
    }

    /**
     * @return Modifiable response headers.
     */
    HttpHeaders headers();

    /**
     * @return Currently set response {@link HttpStatus}, if any.
     */
    Optional<HttpStatus> status();

    /**
     * Sets response status.
     * <p>
     * If a response status is explicitly set by a
     * {@link HttpValidatorHandler}, the associated request will <i>not</i> be
     * passed on to any further validator handlers or a route handler.
     *
     * @param status New response {@link HttpStatus}.
     * @return This response object.
     */
    HttpServiceResponse status(final HttpStatus status);

    /**
     * @return Designated response {@link HttpVersion}.
     */
    HttpVersion version();
}
