package se.arkalix.net;

import se.arkalix.codec.CodecType;
import se.arkalix.codec.MultiEncodable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * An outgoing network message not always required to always have its body
 * codec specified.
 * <p>
 * That an outgoing message type implements this interface indicates that its
 * sender is able to assign the message a suitable codec even if none is
 * explicitly specified by the creator of the message.
 *
 * @param <Self> Implementing class.
 */
public interface MessageOutgoingWithImplicitCodec<Self> extends MessageOutgoing<Self> {
    /**
     * Sets given {@code encodable} as outgoing message body.
     * <p>
     * An attempt will be made to automatically resolve a suitable codec for
     * the {@code encodable}. If the attempt fails the message will not be sent.
     *
     * @param encodable Object to use, in encoded form, as message body.
     * @return This message.
     * @throws NullPointerException If {@code codec} or {@code encodable} is
     *                              {@code null}.
     */
    default Self body(final MultiEncodable encodable) {
        Objects.requireNonNull(encodable, "encodable");
        return body(BodyOutgoing.create(writer -> encodable
            .encodableFor(codecType()
                .orElseThrow(() -> new MessageCodecUnspecified(this)))
            .encode(writer)));
    }

    /**
     * Sets given {@code string} as outgoing message body.
     * <p>
     * An attempt will be made to automatically resolve a suitable codec for
     * the {@code string}. If it fails UTF-8 will be used by default.
     * <p>
     * It becomes the responsibility of the caller to ensure that the message
     * receiver knows how to interpret the body.
     *
     * @param string String to send to message receiver.
     * @return This message.
     * @throws NullPointerException If {@code string} is {@code null}.
     */
    default Self body(final String string) {
        return body(BodyOutgoing.create(writer -> {
            CodecType codecType = codecType().orElse(null);
            Charset charset = null;

            if (codecType != null) {
                charset = codecType.charset().orElse(null);
            }
            if (charset == null) {
                codecType = CodecType.UTF_8;
                charset = StandardCharsets.UTF_8;
            }

            writer.write(string.getBytes(charset));
            return codecType;
        }));
    }
}
