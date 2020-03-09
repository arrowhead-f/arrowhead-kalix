package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.dto.DataWritable;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.HttpVersion;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An outgoing HTTP response, to be sent by an {@link HttpService}.
 */
public interface HttpServiceResponse {
    /**
     * @return Response body, if any has been set.
     */
    Optional<Object> body();

    /**
     * Sets response body, replacing any previously set such.
     * <p>
     * The provided byte array is scheduled for transmission to the response
     * receiver as-is. It becomes the responsibility of the caller to ensure
     * that the {@code "content-type"} header is set appropriately. The
     * {@code "content-length"} header is, however, automatically set to the
     * length of the byte array.
     * <p>
     * If a response body is explicitly set by a
     * {@link HttpValidatorHandler}, the associated request will <i>not</i> be
     * passed on to any further validator handlers or a route handler. If no
     * response status is explicitly set, {@code 400 Bad Request} will be used.
     *
     * @param byteArray Bytes to send to response receiver.
     * @return This response object.
     */
    HttpServiceResponse body(final byte[] byteArray);

    /**
     * Sets response body, replacing any previously set such.
     * <p>
     * The provided writable data transfer object is scheduled for encoding and
     * transmission to the response receiver. Please refer to the Javadoc for
     * the {@code @Writable} annotation for more information about writable
     * data transfer objects.
     * <p>
     * If a response body is explicitly set by a
     * {@link HttpValidatorHandler}, the associated request will <i>not</i> be
     * passed on to any further validator handlers or a route handler. If no
     * response status is explicitly set, {@code 400 Bad Request} will be used.
     *
     * @param body Data transfer object to send to response receiver.
     * @return This response object.
     * @throws NullPointerException If {@code body} is {@code null}.
     * @see eu.arrowhead.kalix.dto.Writable @Writable
     */
    HttpServiceResponse body(final DataWritable body);

    /**
     * Sets response body, replacing any previously set such.
     * <p>
     * The contents of the file at the provided file system path are scheduled
     * for transmission to the response receiver as-is. It becomes the
     * responsibility of the caller to ensure that the {@code "content-type"}
     * header is set appropriately. The {@code "content-length"} header is,
     * however, automatically set to the size of the file.
     * <p>
     * If a response body is explicitly set by a
     * {@link HttpValidatorHandler}, the associated request will <i>not</i> be
     * passed on to any further validator handlers or a route handler. If no
     * response status is explicitly set, {@code 400 Bad Request} will be used.
     *
     * @param path Path to file to send to response receiver.
     * @return This response object.
     * @throws NullPointerException If {@code path} is {@code null}.
     */
    HttpServiceResponse body(final Path path);

    /**
     * Sets response body, replacing any previously set such.
     * <p>
     * The provided string is scheduled for transmission to the response
     * receiver as-is. It becomes the responsibility of the caller to ensure
     * that the {@code "content-type"} header is set appropriately. If no
     * charset is specified in the {@code "content-type"}, one that is
     * acceptable to the response receiver will be used if possible. The
     * {@code "content-length"} header is automatically set to the length of
     * the string.
     * <p>
     * If a response body is explicitly set by a
     * {@link HttpValidatorHandler}, the associated request will <i>not</i> be
     * passed on to any further validator handlers or a route handler. If no
     * response status is explicitly set, {@code 400 Bad Request} will be used.
     *
     * @param string String to send to response receiver.
     * @return This response object.
     * @throws NullPointerException If {@code string} is {@code null}.
     */
    HttpServiceResponse body(final String string);

    /**
     * Removes any currently set response body.
     *
     * @return This response object.
     */
    HttpServiceResponse clearBody();

    /**
     * Removes all currently set headers.
     *
     * @return This response object.
     */
    HttpServiceResponse clearHeaders();

    /**
     * @return Encoding that will or should be used for any response body.
     */
    EncodingDescriptor encoding();

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
