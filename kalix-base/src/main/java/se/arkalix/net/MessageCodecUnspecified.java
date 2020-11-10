package se.arkalix.net;

/**
 * Represents the inability to encode or decode a {@link Message}, caused by no
 * supported codec being specified.
 */
public class MessageCodecUnspecified extends MessageException {
    /**
     * Creates new exception.
     *
     * @param message Offending message.
     */
    public MessageCodecUnspecified(final Message message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "No message codec specified; unable to encode or decode " +
            super.message();
    }
}
