package eu.arrowhead.kalix.net.http;

import eu.arrowhead.kalix.dto.DataWritable;

import java.nio.file.Path;
import java.util.Optional;

public interface HttpBodySender<Self> {
    /**
     * Gets outgoing HTTP body, if any is set.
     *
     * @return Any currently set response body.
     */
    Optional<Object> body();

    /**
     * Sets outgoing HTTP body, replacing any previously set such.
     * <p>
     * The provided byte array is scheduled for transmission to the outgoing
     * body receiver as-is. It becomes the responsibility of the caller to
     * ensure that any {@code "content-type"} header is set appropriately.
     *
     * @param byteArray Bytes to send to outgoing body receiver.
     * @return This.
     */
    Self body(final byte[] byteArray);

    /**
     * Sets outgoing HTTP body, replacing any previously set such.
     * <p>
     * The provided writable data transfer object is scheduled for encoding and
     * transmission to the outgoing body receiver. Please refer to the Javadoc
     * for the {@code @Writable} annotation for more information about writable
     * data transfer objects.
     *
     * @param dto Data transfer object to send to outgoing body receiver.
     * @return This.
     * @throws NullPointerException If {@code body} is {@code null}.
     * @see eu.arrowhead.kalix.dto.Writable @Writable
     */
    Self body(final DataWritable dto);

    /**
     * Sets outgoing HTTP body, replacing any previously set such.
     * <p>
     * The contents of the file at the provided file system path are scheduled
     * for transmission to the outgoing body receiver as-is. It becomes the
     * responsibility of the caller to ensure that the {@code "content-type"}
     * header is set appropriately.
     *
     * @param path Path to file to send to outgoing body receiver.
     * @return This.
     * @throws NullPointerException If {@code path} is {@code null}.
     */
    Self body(final Path path);

    /**
     * Sets outgoing HTTP body, replacing any previously set such.
     * <p>
     * The provided string is scheduled for transmission to the outgoing HTTP
     * receiver as-is. It becomes the responsibility of the caller to ensure
     * that the {@code "content-type"} header is set appropriately. If no
     * charset is specified in the {@code "content-type"}, one that is
     * acceptable to the outgoing body receiver will be used if possible.
     *
     * @param string String to send to outgoing body receiver.
     * @return This.
     * @throws NullPointerException If {@code string} is {@code null}.
     */
    Self body(final String string);

    /**
     * Removes any currently set outgoing HTTP body.
     *
     * @return This.
     */
    Self clearBody();
}
