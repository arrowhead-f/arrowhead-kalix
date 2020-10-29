package se.arkalix.net.http;

import io.netty.handler.codec.http.HttpUtil;
import se.arkalix.encoding.Encoding;
import se.arkalix.net.http._internal.HttpMediaTypes;
import se.arkalix.net.MessageEncodingInvalid;
import se.arkalix.net.MessageIncoming;

import java.nio.charset.Charset;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

/**
 * An incoming HTTP message.
 *
 * @param <Self> Implementing class.
 */
public interface HttpIncoming<Self> extends HttpMessage<Self>, MessageIncoming {
    @Override
    default Optional<Charset> charset() {
        return header(CONTENT_TYPE).flatMap(contentType -> Optional.ofNullable(HttpUtil.getCharset(contentType, null)));
    }

    @Override
    default Optional<Encoding> encoding() {
        final var contentType = headers().get(CONTENT_TYPE).orElse(null);
        if (contentType == null || contentType.length() == 0) {
            return Optional.empty();
        }
        final var encoding = HttpMediaTypes.encodingFromContentType(contentType)
            .orElseThrow(() -> new MessageEncodingInvalid(this, contentType));

        return Optional.of(encoding);
    }

    /**
     * Gets HTTP version used by incoming message.
     *
     * @return HTTP version.
     */
    HttpVersion version();
}
