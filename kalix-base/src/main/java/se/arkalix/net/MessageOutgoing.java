package se.arkalix.net;

import se.arkalix.dto.DtoWritable;
import se.arkalix.dto.DtoWritableAs;

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
     * Gets outgoing message body, if any is set.
     *
     * @return Any currently set response body.
     */
    Optional<Object> body();

    /**
     * Sets outgoing message body, replacing any previously set such.
     * <p>
     * The provided byte array is scheduled for transmission to the outgoing
     * body receiver as-is. It becomes the responsibility of the caller to
     * ensure that the message receiver knows how to interpret the body.
     *
     * @param byteArray Bytes to send to message receiver.
     * @return This message.
     */
    Self body(final byte[] byteArray);

    /**
     * Sets outgoing message body, replacing any previously set such.
     * <p>
     * The provided writable data transfer object is scheduled for encoding,
     * using the given {@code encoding}, and transmission to the message
     * receiver. Please refer to the Javadoc for the {@code @DtoWritableAs}
     * annotation for more information about writable data transfer objects.
     *
     * @param encoding Encoding to use when encoding {@code data}, or {@code
     *                 null} if an attempt is to be made to resolve the
     *                 encoding automatically.
     * @param data     Data transfer object to send to message receiver.
     * @return This message.
     * @throws NullPointerException If {@code data} is {@code null}.
     * @see DtoWritableAs @DtoWritableAs
     */
    Self body(final ToEncoding encoding, final DtoWritable data);

    /**
     * Sets outgoing message body, replacing any previously set such.
     * <p>
     * The provided array of writable data transfer objects are scheduled for
     * encoding, using the given {@code encoding}, and transmission to the
     * message receiver. Please refer to the Javadoc for the
     * {@code @DtoWritableAs} annotation for more information about writable
     * data transfer objects.
     *
     * @param encoding Encoding to use when encoding {@code data}, or {@code
     *                 null} if an attempt is to be made to resolve the
     *                 encoding automatically.
     * @param data     Data transfer objects to send to message receiver.
     * @return This message.
     * @throws NullPointerException If {@code data} is {@code null}.
     * @see DtoWritableAs @DtoWritableAs
     */
    default Self body(final ToEncoding encoding, final DtoWritable... data) {
        return body(encoding, List.of(data));
    }

    /**
     * Sets outgoing message body, replacing any previously set such.
     * <p>
     * The provided list of writable data transfer objects are scheduled for
     * encoding, using the given {@code encoding}, and transmission to the
     * message receiver. Please refer to the Javadoc for the
     * {@code @DtoWritableAs} annotation for more information about writable
     * data transfer objects.
     *
     * @param encoding Encoding to use when encoding {@code data}, or {@code
     *                 null} if an attempt is to be made to resolve the
     *                 encoding automatically.
     * @param data     List of data transfer objects to send to message
     *                 receiver.
     * @return This message.
     * @throws NullPointerException If {@code data} is {@code null}.
     * @see DtoWritableAs @DtoWritableAs
     */
    <L extends List<? extends DtoWritable>> Self body(final ToEncoding encoding, final L data);

    /**
     * Sets outgoing message body, replacing any previously set such.
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
    Self body(final Path path);

    /**
     * Sets outgoing message body, replacing any previously set such.
     * <p>
     * The provided string is scheduled for transmission to the outgoing
     * message receiver encoded using an automatically selected character set.
     * It becomes the responsibility of the caller to ensure that the message
     * receiver knows  how to interpret the body, unless it is to be received
     * only as an unstructured text.
     *
     * @param string String to send to message receiver.
     * @return This message.
     * @throws NullPointerException If {@code string} is {@code null}.
     */
    default Self body(final String string) {
        return body(null, string);
    }

    /**
     * Sets outgoing message body, replacing any previously set such.
     * <p>
     * The provided string is scheduled for transmission to the outgoing
     * message receiver encoded using specified {@code charset}. It becomes the
     * responsibility of the caller to ensure that the message receiver knows
     * how to interpret the body, unless it is to be received only as an
     * unstructured text.
     *
     * @param charset Character set to use for encoding {@code string}.
     * @param string  String to send to message receiver.
     * @return This message.
     * @throws NullPointerException If {@code string} is {@code null}.
     */
    Self body(final Charset charset, final String string);

    /**
     * Removes any currently set outgoing message body.
     *
     * @return This message.
     */
    Self clearBody();
}
