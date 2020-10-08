package se.arkalix.net.http;

import java.util.Optional;

/**
 * An outgoing HTTP response.
 *
 * @param <Self> Implementing class.
 */
public interface HttpOutgoingResponse<Self> extends HttpOutgoing<Self> {
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
    Self status(final HttpStatus status);
}
