package se.arkalix.net;

import java.util.Objects;

/**
 * Represents the inability to encode or decode a {@link Message}, caused by
 * an encoding being specified with an unexpected format.
 */
public class MessageEncodingInvalid extends MessageException {
    private final Object encoding;

    /**
     * Creates new exception.
     *
     * @param message  Offending message.
     * @param encoding Unexpectedly formatted encoding.
     */
    public MessageEncodingInvalid(final Message message, final Object encoding) {
        this(message, encoding, null);
    }

    /**
     * Creates new exception.
     *
     * @param message  Offending message.
     * @param encoding Unexpectedly formatted encoding.
     * @param cause    Description of formatting error, if any.
     */
    public MessageEncodingInvalid(final Message message, final Object encoding, final Throwable cause) {
        super(message, cause);
        this.encoding = Objects.requireNonNull(encoding, "encoding");
    }

    /**
     * Gets encoding causing this exception to be thrown.
     *
     * @return Invalid encoding descriptor.
     */
    public Object encoding() {
        return encoding;
    }

    @Override
    public String getMessage() {
        return "Message encoding \"" + encoding + "\" has an unexpected " +
            "format; unable to encode or decode " + super.message();
    }
}
