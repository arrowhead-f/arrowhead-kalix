package se.arkalix.net.http;

import se.arkalix.codec.MediaType;
import se.arkalix.codec.ToCodecType;
import se.arkalix.net.BodyOutgoing;
import se.arkalix.net.MessageOutgoing;

import java.util.Optional;

/**
 * An outgoing HTTP message.
 *
 * @param <Self> Implementing class.
 */
public interface HttpOutgoing<Self> extends HttpMessage<Self>, MessageOutgoing<Self> {
    @Override
    default Self codecType(final ToCodecType codecType) {
        return contentType(codecType instanceof MediaType
            ? (MediaType) codecType
            : MediaType.getOrCreate(codecType.toCodecType()));
    }

    /**
     * Sets content-type used to represent the body of this message.
     * <p>
     * This method only needs to be called if the content-type cannot be
     * determined automatically from the set {@link #body(BodyOutgoing) body},
     * such as when using {@link #body(byte[])}. Note that whatever object is
     * sending this message might set the codec automatically if required.
     *
     * @param mediaType Media type to set.
     * @return This message.
     */
    Self contentType(final MediaType mediaType);

    /**
     * Sets header with {@code name} to given value.
     *
     * @param name  Name of header. Case is ignored. Prefer lowercase.
     * @param value Desired header value.
     * @return This request.
     */
    Self header(final CharSequence name, final CharSequence value);

    /**
     * @return Currently set HTTP version, if any.
     */
    Optional<HttpVersion> version();

    /**
     * Sets HTTP version.
     * <p>
     * Note that only HTTP/1.0 and HTTP/1.1 are supported by this version of
     * Arrowhead Kalix. If no version is set, HTTP/1.1 is used by default.
     *
     * @param version Desired HTTP version.
     * @return This request.
     */
    Self version(final HttpVersion version);
}
