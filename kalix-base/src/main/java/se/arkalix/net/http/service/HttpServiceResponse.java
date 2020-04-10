package se.arkalix.net.http.service;

import se.arkalix.dto.DtoWritable;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.net.http.HttpBodySender;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.HttpVersion;

import java.util.Optional;

/**
 * An outgoing HTTP response, to be sent by an {@link HttpService}.
 */
public interface HttpServiceResponse extends HttpBodySender<HttpServiceResponse> {
    /**
     * Sets outgoing HTTP body, replacing any previously set such.
     * <p>
     * The provided writable data transfer object is scheduled for encoding,
     * using the encoding chosen automatically for this response, and
     * transmission to the receiver of the body. Please refer to the Javadoc
     * for the {@code @DtoWritableAs} annotation for more information about writable
     * data transfer objects.
     *
     * @param data Data transfer object to send to receiver of the body.
     * @return This.
     * @throws NullPointerException If {@code encoding} or {@code body} is
     *                              {@code null}.
     * @see DtoWritableAs @DtoWritableAs
     */
    HttpServiceResponse body(final DtoWritable data);

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
