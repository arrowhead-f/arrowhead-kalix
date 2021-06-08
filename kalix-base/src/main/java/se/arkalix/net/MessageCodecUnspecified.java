package se.arkalix.net;

/**
 * Represents the inability to encode or decode a {@link Message}, caused by no
 * supported codec being specified.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces unless debug-level logging is enabled for this class</i>. If an HTTP
 * request causes an error that should always generate a stack trace, some
 * other exception type should be used instead.
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
