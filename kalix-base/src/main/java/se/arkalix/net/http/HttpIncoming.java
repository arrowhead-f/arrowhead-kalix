package se.arkalix.net.http;

import se.arkalix.encoding.Encoding;
import se.arkalix.encoding.MediaType;
import se.arkalix.net.MessageEncodingMisspecified;
import se.arkalix.net.MessageIncoming;

import java.util.Optional;

/**
 * An incoming HTTP message.
 *
 * @param <Self> Implementing class.
 */
public interface HttpIncoming<Self> extends HttpMessage<Self>, MessageIncoming {
    @Override
    default Optional<Encoding> encoding() {
        try {
            return headers().getAs("content-type", MediaType::valueOf)
                .map(MediaType::toEncoding);
        }
        catch (final HttpHeaderInvalid exception) {
            throw new MessageEncodingMisspecified(this, exception.value(), exception);
        }
    }

    /**
     * Gets HTTP version used by incoming message.
     *
     * @return HTTP version.
     */
    HttpVersion version();
}
