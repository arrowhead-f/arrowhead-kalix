package se.arkalix.net.http;

import se.arkalix.codec.CodecType;
import se.arkalix.codec.MediaType;
import se.arkalix.net.MessageCodecMisspecified;
import se.arkalix.net.MessageIncoming;

import java.util.Optional;

/**
 * An incoming HTTP message.
 *
 * @param <Self> Implementing class.
 */
public interface HttpIncoming<Self> extends HttpMessage<Self>, MessageIncoming {
    @Override
    default Optional<MediaType> contentType() {
        try {
            return headers().getAs("content-type", MediaType::valueOf);
        }
        catch (final HttpHeaderInvalid exception) {
            throw new MessageCodecMisspecified(this, exception.value(), exception);
        }
    }

    /**
     * Gets HTTP version used by incoming message.
     *
     * @return HTTP version.
     */
    HttpVersion version();
}
