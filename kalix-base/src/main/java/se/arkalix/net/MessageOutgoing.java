package se.arkalix.net;

import se.arkalix.encoding.Encodable;
import se.arkalix.encoding.Encoding;
import se.arkalix.encoding.MultiEncodable;
import se.arkalix.encoding.ToEncoding;

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
     * Sets name of encoding used to represent the body of this message.
     * <p>
     * This method only needs to be called if the encoding cannot be determined
     * automatically from the set {@link #body(BodyOutgoing) body}, such as
     * when using {@link #body(byte[])}. Note that the message sender might set
     * the encoding automatically when required.
     *
     * @param encoding Encoding to set.
     * @return This message.
     */
    Self encoding(final ToEncoding encoding);

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
            return Encoding.NONE;
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
     * using given {@code encoding}.
     *
     * @param encodable Object to use, in encoded form, as message body.
     * @param encoding  Encoding to use when encoding {@code encodable}.
     * @return This message.
     * @throws NullPointerException If {@code encoding} or {@code encodable} is
     *                              {@code null}.
     */
    default Self body(final MultiEncodable encodable, final ToEncoding encoding) {
        Objects.requireNonNull(encodable, "encodable");
        Objects.requireNonNull(encoding, "encoding");

        final var encoding0 = encoding.toEncoding();
        return body(BodyOutgoing.create(writer -> {
            encodable.encode(writer, encoding0);
            return Encoding.NONE;
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
     * @param charset Character set to use for encoding {@code string}.
     * @return This message.
     * @throws NullPointerException If {@code string} or {@code charset} is
     *                              {@code null}.
     */
    default Self body(final String string, final Charset charset) {
        Objects.requireNonNull(string, "string");
        Objects.requireNonNull(charset, "charset");

        final var encoding = Encoding.getOrRegister(charset);
        return body(BodyOutgoing.create(writer -> {
            writer.write(string.getBytes(charset));
            return encoding;
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
