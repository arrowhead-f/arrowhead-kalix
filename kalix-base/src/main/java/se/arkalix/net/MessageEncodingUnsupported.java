package se.arkalix.net;

import se.arkalix.encoding.Encoding;
import se.arkalix.encoding.EncodingUnsupported;

import java.util.Objects;

/**
 * Represents the inability to encode or decode a {@link Message}, caused by
 * an unsupported encoding being specified.
 */
public class MessageEncodingUnsupported extends MessageException {
    private final Encoding encoding;

    /**
     * Creates new exception.
     *
     * @param message Offending message.
     * @param cause   Unsupported encoding exception causing this exception to
     *                be thrown.
     */
    public MessageEncodingUnsupported(final Message message, final EncodingUnsupported cause) {
        super(message, cause);
        this.encoding = Objects.requireNonNull(cause).encoding();
    }

    /**
     * Gets name of unsupported encoding causing this exception to be thrown.
     *
     * @return Unsupported encoding descriptor.
     */
    public Encoding encoding() {
        return encoding;
    }

    @Override
    public String getMessage() {
        return "Unsupported message encoding \"" + encoding + "\"; unable " +
            "to encode or decode " + super.message();
    }
}
