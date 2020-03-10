package eu.arrowhead.kalix.net.http.client;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.net.http.*;

import java.util.List;
import java.util.Optional;

/**
 * An incoming  HTTP response.
 */
public interface HttpClientResponse extends HttpBodyReceiver {
    /**
     * @return Encoding used to encode the body, if any, of this response.
     * Note that the encoding is a reflection of what Arrowhead service
     * interface was selected for the response rather than any specifics about
     * the response itself. This means that an encoding descriptor will be
     * available even if the response has no body.
     */
    EncodingDescriptor encoding();

    /**
     * Gets a response header value by name.
     *
     * @param name Name of header. Case insensitive. Prefer lowercase.
     * @return Header value, if any.
     */
    default Optional<String> header(final CharSequence name) {
        return headers().get(name);
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
     * @return Response headers.
     */
    HttpHeaders headers();

    /**
     * @return Response status.
     */
    HttpStatus status();

    /**
     * @return Response version.
     */
    HttpVersion version();
}
