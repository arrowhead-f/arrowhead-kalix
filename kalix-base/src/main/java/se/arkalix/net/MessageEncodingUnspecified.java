package se.arkalix.net;

/**
 * Represents the inability to encode or decode a {@link Message}, caused by no
 * supported encoding being specified.
 */
public class MessageEncodingUnspecified extends MessageException {
    /**
     * Creates new exception.
     *
     * @param message Offending message.
     */
    public MessageEncodingUnspecified(final Message message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "No message encoding specified; unable to encode or decode " +
            super.message();
    }
}
