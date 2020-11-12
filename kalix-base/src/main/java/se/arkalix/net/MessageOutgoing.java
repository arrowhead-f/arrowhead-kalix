package se.arkalix.net;

import se.arkalix.codec.Encodable;
import se.arkalix.codec.CodecType;
import se.arkalix.codec.MultiEncodable;
import se.arkalix.codec.ToCodecType;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * An outgoing network message.
 *
 * @param <Self> Implementing class.
 */
@SuppressWarnings("UnusedReturnValue")
public interface MessageOutgoing<Self> extends Message {
    /**
     * Sets type of codec used to represent the body of this message.
     * <p>
     * This method only needs to be called if the codec cannot be determined
     * automatically from the set {@link #body(BodyOutgoing) body}, such as
     * when using {@link #body(byte[])}. Note that whatever object is sending
     * this message might set the codec automatically if required.
     *
     * @param codecType Codec to set.
     * @return This message.
     */
    Self codecType(final ToCodecType codecType);

    /**
     * Gets outgoing message body, if any is set.
     *
     * @return Any currently set response body.
     */
    Optional<BodyOutgoing> body();

    /**
     * Sets outgoing message body, replacing any previously set.
     *
     * @param body Desired message body.
     * @return This message.
     */
    Self body(BodyOutgoing body);

    /**
     * Sets given {@code byteArray} as outgoing message body.
     * <p>
     * It becomes the responsibility of the caller to ensure that the message
     * receiver knows how to interpret the body.
     *
     * @param byteArray Bytes to send to message receiver.
     * @return This message.
     * @throws NullPointerException If {@code byteArray} is {@code null}.
     */
    default Self body(final byte[] byteArray) {
        Objects.requireNonNull(byteArray, "byteArray");
        return body(BodyOutgoing.create(writer -> {
            writer.write(byteArray);
            return CodecType.NONE;
        }));
    }

    /**
     * Sets given {@code encodable} as outgoing message body.
     *
     * @param encodable Object to use, in encoded form, as message body.
     * @return This message.
     * @throws NullPointerException If {@code encodable} is {@code null}.
     */
    default Self body(final Encodable encodable) {
        return body(BodyOutgoing.create(encodable));
    }

    /**
     * Sets given {@code encodable} as outgoing message body, to be encoded
     * using given {@code codec}.
     *
     * @param encodable Object to use, in encoded form, as message body.
     * @param codec     Codec to use when codec {@code encodable}.
     * @return This message.
     * @throws NullPointerException If {@code codec} or {@code encodable} is
     *                              {@code null}.
     */
    default Self body(final MultiEncodable encodable, final ToCodecType codec) {
        Objects.requireNonNull(encodable, "encodable");
        Objects.requireNonNull(codec, "codec");

        final var codec0 = codec.toCodecType();
        return body(BodyOutgoing.create(writer -> {
            encodable.encode(writer, codec0);
            return CodecType.NONE;
        }));
    }

    /**
     * Sets file at given {@code path} as outgoing message body.
     * <p>
     * The contents of the file at the provided file system path are scheduled
     * for transmission to the message receiver as-is. It becomes the
     * responsibility of the caller to ensure that the message receiver knows
     * how to interpret the body.
     *
     * @param path Path to file to send to message receiver.
     * @return This message.
     * @throws NullPointerException If {@code path} is {@code null}.
     */
    default Self body(final Path path) {
        return body(BodyOutgoing.create(path));
    }

    /**
     * Sets given {@code string} as outgoing message body.
     * <p>
     * It becomes the responsibility of the caller to ensure that the message
     * receiver knows how to interpret the body.
     *
     * @param string  String to send to message receiver.
     * @param charset Character set to use for codec {@code string}.
     * @return This message.
     * @throws NullPointerException If {@code string} or {@code charset} is
     *                              {@code null}.
     */
    default Self body(final String string, final Charset charset) {
        Objects.requireNonNull(string, "string");
        Objects.requireNonNull(charset, "charset");

        final var codec = CodecType.getOrRegister(charset);
        return body(BodyOutgoing.create(writer -> {
            writer.write(string.getBytes(charset));
            return codec;
        }));
    }

    /**
     * Removes any currently set outgoing message body.
     *
     * @return This message.
     */
    default Self clearBody() {
        return body((BodyOutgoing) null);
    }
}
