package se.arkalix.net;

import se.arkalix.dto.DtoWritable;
import se.arkalix.dto.DtoWritableAs;

import java.util.List;

/**
 * An outgoing network message not explicitly required to have its encoding
 * specified when its body is being a {@link DtoWritable}.
 * <p>
 * That an outgoing message type implements this interface indicates that its
 * sender is able to assign the message a suitable encoding even if none is
 * explicitly specified by the creator of the message.
 *
 * @param <Self> Implementing class.
 */
public interface MessageOutgoingWithImplicitEncoding<Self> extends MessageOutgoing<Self> {
    /**
     * Sets outgoing message body, replacing any previously set such.
     * <p>
     * The provided writable data transfer object is scheduled for encoding and
     * transmission to the message receiver. An attempt will be made to resolve
     * a suitable encoding automatically. Please refer to the Javadoc for the
     * {@code @DtoWritableAs} annotation for more information about writable
     * data transfer objects.
     *
     * @param data Data transfer object to send to message receiver.
     * @return This message.
     * @throws NullPointerException If {@code data} is {@code null}.
     * @see DtoWritableAs @DtoWritableAs
     */
    default Self body(final DtoWritable data) {
        return body(null, data);
    }

    /**
     * Sets outgoing message body, replacing any previously set such.
     * <p>
     * The provided array of writable data transfer objects is scheduled for
     * encoding and transmission to the message receiver. An attempt will be
     * made to resolve a suitable encoding automatically. Please refer to the
     * Javadoc for the {@code @DtoWritableAs} annotation for more information
     * about writable data transfer objects.
     *
     * @param data Array of data transfer objects to send to message receiver.
     * @return This message.
     * @throws NullPointerException If {@code data} is {@code null}.
     * @see DtoWritableAs @DtoWritableAs
     */
    default Self body(final DtoWritable... data) {
        return body(null, data);
    }

    /**
     * Sets outgoing message body, replacing any previously set such.
     * <p>
     * The provided list of writable data transfer objects is scheduled for
     * encoding and transmission to the message receiver. An attempt will be
     * made to resolve a suitable encoding automatically. Please refer to the
     * Javadoc for the {@code @DtoWritableAs} annotation for more information
     * about writable data transfer objects.
     *
     * @param data List of data transfer objects to send to message receiver.
     * @return This message.
     * @throws NullPointerException If {@code data} is {@code null}.
     * @see DtoWritableAs @DtoWritableAs
     */
    default <L extends List<? extends DtoWritable>> Self body(final L data) {
        return body(null, data);
    }
}
