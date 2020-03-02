package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.dto.FormatWritable;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.HttpVersion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * An outgoing HTTP response, to be sent by an {@link HttpService}.
 */
public class HttpServiceResponse {
    /**
     * Sets response body, replacing any previously set such.
     * <p>
     * The provided byte array is scheduled for transmission to the response
     * receiver as-is. It becomes the responsibility of the caller to ensure
     * that the {@code "content-type"} header is set appropriately. The
     * {@code "content-length"} header is, however, automatically set to the
     * length of the byte array.
     *
     * @param bytes Bytes to send to response receiver.
     * @return This response object.
     */
    public HttpServiceResponse body(final byte[] bytes) {
        return this;
    }

    /**
     * Sets response body, replacing any previously set such.
     * <p>
     * The provided writable data transfer object is scheduled for encoding and
     * transmission to the response receiver. Please refer to the Javadoc for
     * the {@code @Writable} annotation for more information about writable
     * data transfer objects.
     *
     * @param body Data transfer object to send to response receiver.
     * @return This response object.
     * @see eu.arrowhead.kalix.dto.Writable @Writable
     */
    public HttpServiceResponse body(final FormatWritable body) {
        // TODO.
        return this;
    }

    /**
     * Sets response body, replacing any previously set such.
     * <p>
     * The contents of the file at the provided file system path are scheduled
     * for transmission to the response receiver as-is. It becomes the
     * responsibility of the caller to ensure that the {@code "content-type"}
     * header is set appropriately. The {@code "content-length"} header is,
     * however, automatically set to the size of the file.
     *
     * @param path Path to file to send to response receiver.
     * @return This response object.
     * @throws IOException If no file exists at {@code path}, or if it cannot
     *                     be read for any other reason.
     */
    public HttpServiceResponse body(final Path path) throws IOException {
        return this;
    }

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
     *
     * @param string String to send to response receiver.
     * @return This response object.
     */
    public HttpServiceResponse body(final String string) {
        return this;
    }

    /**
     * Prevents any further {@link HttpFilter}s or {@link HttpRoute} from
     * processing this response.
     */
    public HttpServiceResponse eject() {
        // TODO.
        return this;
    }

    /**
     * Gets a response header value by name.
     *
     * @param name Name of header. Case sensitive. Should be lower-case.
     * @return Header value, if any.
     */
    public Optional<String> header(final String name) {
        return null;
    }

    /**
     * Sets a response header.
     *
     * @param name  Name of header. Case sensitive. Should be lower-case.
     * @param value Header value.
     * @return This response object.
     */
    public HttpServiceResponse header(final String name, final String value) {
        // TODO.
        return this;
    }

    /**
     * @return Mutable {@link Map} of all response headers.
     */
    public HttpHeaders headers() {
        return null;
    }

    /**
     * Replaces all existing response headers.
     *
     * @param headers New map of response headers.
     * @return This response object.
     */
    public HttpServiceResponse headers(final HttpHeaders headers) {
        return null;
    }

    /**
     * @return Currently set response {@link HttpStatus}.
     */
    public HttpStatus status() {
        return null;
    }

    /**
     * @param status New response {@link HttpStatus}.
     * @return This response object.
     */
    public HttpServiceResponse status(final HttpStatus status) {
        // TODO.
        return this;
    }

    /**
     * @return Currently set response {@link HttpVersion}.
     */
    public HttpVersion version() {
        return null;
    }

    /**
     * @param version New response {@link HttpVersion}.
     * @return This response object.
     */
    public HttpServiceResponse version(final HttpVersion version) {
        // TODO.
        return this;
    }
}
