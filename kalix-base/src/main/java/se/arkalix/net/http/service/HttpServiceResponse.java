package se.arkalix.net.http.service;

import se.arkalix.net.MessageOutgoingWithDefaultEncoding;
import se.arkalix.net.http.HttpOutgoing;
import se.arkalix.net.http.HttpStatus;

import java.util.Optional;

/**
 * An outgoing HTTP response, to be sent by an {@link HttpService}.
 */
public interface HttpServiceResponse
    extends HttpOutgoing<HttpServiceResponse>, MessageOutgoingWithDefaultEncoding<HttpServiceResponse>
{
    /**
     * Removes all headers from this response.
     *
     * @return This response.
     */
    default HttpServiceResponse clearHeaders() {
        headers().clear();
        return this;
    }

    /**
     * @return Currently set response {@link HttpStatus}, if any.
     */
    Optional<HttpStatus> status();

    /**
     * Sets response status.
     *
     * @param status New response {@link HttpStatus}.
     * @return This response object.
     */
    HttpServiceResponse status(final HttpStatus status);
}
