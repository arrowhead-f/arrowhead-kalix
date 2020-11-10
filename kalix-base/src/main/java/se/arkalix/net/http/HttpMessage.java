package se.arkalix.net.http;

import se.arkalix.codec.CodecType;
import se.arkalix.codec.MediaType;
import se.arkalix.net.Message;

import java.util.List;
import java.util.Optional;

/**
 * An arbitrary HTTP message.
 *
 * @param <Self> Implementing class.
 */
public interface HttpMessage<Self> extends Message {
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

    /**
     * Removes all headers from this response.
     *
     * @return This response.
     */
    Self clearHeaders();

    @Override
    default Optional<CodecType> codecType() {
        return contentType().map(MediaType::toCodecType);
    }

    /**
     * Gets content type associated with this HTTP message, if any.
     *
     * @return Content type associated with message, if any.
     */
    Optional<MediaType> contentType();
}
