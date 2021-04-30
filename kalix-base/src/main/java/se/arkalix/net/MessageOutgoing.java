package se.arkalix.net;

import se.arkalix.codec.*;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
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
        if (byteArray == null) {
            throw new NullPointerException("byteArray");
        }
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
     * using given {@code toCodecType}.
     *
     * @param encodable   Object to use, in encoded form, as message body.
     * @param toCodecType Codec to provide to {@code encodable}.
     * @return This message.
     * @throws NullPointerException If {@code toCodecType} or {@code encodable}
     *                              is {@code null}.
     */
    default Self body(final MultiEncodable encodable, final ToCodecType toCodecType) {
        if (encodable == null) {
            throw new NullPointerException("encodable");
        }
        if (toCodecType == null) {
            throw new NullPointerException("codec");
        }

        final var codecType = toCodecType.toCodecType();
        return body(BodyOutgoing.create(writer -> encodable
            .encodableFor(codecType)
            .encode(writer)));
    }

    /**
     * Sets given {@code encodables} as outgoing message body, to be encoded
     * using given {@code toCodecType}.
     * <p>
     * This method can only succeed for codecs that both support anonymous
     * lists and are explicitly listed as supported by {@link
     * MultiEncodableForLists#supportedEncodings()}.
     *
     * @param encodables  Objects to use, in encoded form, as message body.
     * @param toCodecType Codec to use when codec {@code encodable}.
     * @return This message.
     * @throws NullPointerException If {@code toCodecType} or {@code encodables}
     *                              is {@code null}.
     */
    default Self body(final List<MultiEncodable> encodables, final ToCodecType toCodecType) {
        if (encodables == null) {
            throw new NullPointerException("encodables");
        }
        if (toCodecType == null) {
            throw new NullPointerException("codec");
        }

        final var codec0 = toCodecType.toCodecType();
        return body(BodyOutgoing.create(writer -> MultiEncodableForLists.of(encodables)
            .encodableFor(codec0)
            .encode(writer)));
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
        if (string == null) {
            throw new NullPointerException("string");
        }
        if (charset == null) {
            throw new NullPointerException("charset");
        }

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
