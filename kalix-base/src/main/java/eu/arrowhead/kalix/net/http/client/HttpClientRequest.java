package eu.arrowhead.kalix.net.http.client;

import eu.arrowhead.kalix.dto.DataWritable;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpVersion;
import eu.arrowhead.kalix.net.http.service.HttpServiceResponse;

import java.nio.file.Path;
import java.util.*;

/**
 * An outgoing HTTP request.
 */
public interface HttpClientRequest {
    /**
     * @return Currently set request body, if any.
     */
    Optional<Object> body();

    /**
     * Sets request body, replacing any previously set such.
     * <p>
     * The provided byte array is scheduled for transmission to the request
     * receiver as-is. It becomes the responsibility of the caller to ensure
     * that the {@code "content-type"} header is set appropriately. The
     * {@code "content-length"} header is, however, automatically set to the
     * length of the byte array.
     *
     * @param byteArray Bytes to send to request receiver.
     * @return This request object.
     */
    HttpServiceResponse body(final byte[] byteArray);

    /**
     * Sets request body, replacing any previously set such.
     * <p>
     * The provided writable data transfer object is scheduled for encoding and
     * transmission to the request receiver. Please refer to the Javadoc for
     * the {@code @Writable} annotation for more information about writable
     * data transfer objects.
     *
     * @param body Data transfer object to send to request receiver.
     * @return This request object.
     * @throws NullPointerException If {@code body} is {@code null}.
     * @see eu.arrowhead.kalix.dto.Writable @Writable
     */
    HttpServiceResponse body(final DataWritable body);

    /**
     * Sets request body, replacing any previously set such.
     * <p>
     * The contents of the file at the provided file system path are scheduled
     * for transmission to the request receiver as-is. It becomes the
     * responsibility of the caller to ensure that the {@code "content-type"}
     * header is set appropriately. The {@code "content-length"} header is,
     * however, automatically set to the size of the file.
     *
     * @param path Path to file to send to request receiver.
     * @return This request object.
     * @throws NullPointerException If {@code path} is {@code null}.
     */
    HttpServiceResponse body(final Path path);

    /**
     * Sets request body, replacing any previously set such.
     * <p>
     * The provided string is scheduled for transmission to the request
     * receiver as-is. It becomes the responsibility of the caller to ensure
     * that the {@code "content-type"} header is set appropriately. If no
     * charset is specified in the {@code "content-type"}, one that is
     * acceptable to the request receiver will be used if possible. The
     * {@code "content-length"} header is automatically set to the length of
     * the string.
     *
     * @param string String to send to request receiver.
     * @return This request object.
     * @throws NullPointerException If {@code string} is {@code null}.
     */
    HttpServiceResponse body(final String string);

    /**
     * Gets value of first header with given {@code name}, if any such.
     *
     * @param name Name of header. Case is ignored. Prefer lowercase.
     * @return Header value, or {@code null}.
     */
    default Optional<String> header(final CharSequence name) {
        return headers().get(name);
    }

    /**
     * Sets header with {@code name} to given value.
     *
     * @param name  Name of header. Case is ignored. Prefer lowercase.
     * @param value Desired header value.
     * @return This request.
     */
    default HttpClientRequest header(final CharSequence name, final CharSequence value) {
        headers().set(name, value);
        return this;
    }

    /**
     * Gets all header values associated with given {@code name}, if any.
     *
     * @param name Name of header. Case is ignored. Prefer lowercase.
     * @return Header values. May be an empty list.
     */
    default List<String> headers(final CharSequence name) {
        return headers().getAll(name);
    }

    /**
     * @return <i>Modifiable</i> map of all request headers.
     */
    HttpHeaders headers();

    /**
     * Gets first query parameter with given name, if any such.
     *
     * @param name Name of query parameter. Case sensitive.
     * @return Query parameter value, if a corresponding parameter name exists.
     */
    default Optional<String> queryParameter(final String name) {
        final var values = queryParameters().get(name);
        return Optional.ofNullable(values.size() > 0 ? values.get(0) : null);
    }

    /**
     * Sets query parameter pair, replacing all previous such with the same
     * name.
     *
     * @param name  Name of query parameter. Case sensitive.
     * @param value Desired parameter value.
     * @return Query parameter value, if a corresponding parameter name exists.
     */
    default HttpClientRequest queryParameter(final String name, final CharSequence value) {
        final var list = new ArrayList<String>(1);
        list.add(value.toString());
        queryParameters().put(name, list);
        return this;
    }

    /**
     * @return Modifiable map of query parameters.
     */
    Map<String, List<String>> queryParameters();

    /**
     * @return HTTP version used by request.
     */
    HttpVersion version();
}
