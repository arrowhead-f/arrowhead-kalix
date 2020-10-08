package se.arkalix.net.http;

import se.arkalix.net.Message;

import java.util.List;
import java.util.Optional;

/**
 * An arbitrary HTTP message.
 */
public interface HttpMessage extends Message {
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
}
