package se.arkalix.net;

import se.arkalix.encoding.Encoding;
import se.arkalix.encoding.MultiEncodable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * An outgoing network message not always required to always have its body
 * encoding specified.
 * <p>
 * That an outgoing message type implements this interface indicates that its
 * sender is able to assign the message a suitable encoding even if none is
 * explicitly specified by the creator of the message.
 *
 * @param <Self> Implementing class.
 */
public interface MessageOutgoingWithImplicitEncoding<Self> extends MessageOutgoing<Self> {
    /**
     * Sets given {@code encodable} as outgoing message body.
     * <p>
     * An attempt will be made to automatically resolve a suitable encoding for
     * the {@code encodable}. If the attempt fails the message will not be sent.
     *
     * @param encodable Object to use, in encoded form, as message body.
     * @return This message.
     * @throws NullPointerException If {@code encoding} or {@code encodable} is
     *                              {@code null}.
     */
    default Self body(final MultiEncodable encodable) {
        Objects.requireNonNull(encodable, "encodable");
        return body(BodyOutgoing.create(writer -> {
            final var encoding0 = encoding()
                .orElseThrow(() -> new MessageEncodingUnspecified(this));

            encodable.encodeUsing(writer, encoding0);

            return Optional.of(encoding0);
        }));
    }

    /**
     * Sets given {@code string} as outgoing message body.
     * <p>
     * An attempt will be made to automatically resolve a suitable encoding for
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
            Encoding encoding = encoding().orElse(null);
            Charset charset = null;

            if (encoding != null) {
                charset = encoding.charset().orElse(null);
            }
            if (charset == null) {
                encoding = Encoding.UTF_8;
                charset = StandardCharsets.UTF_8;
            }

            writer.write(string.getBytes(charset));
            return Optional.of(encoding);
        }));
    }
}
