package se.arkalix.net;

/**
 * Represents a {@link Message} containing more data after the first valid
 * syntactic construct in its body.
 * <p>
 * The presence of additional data after the first message body may indicate
 * that the message header is faulty.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces unless debug-level logging is enabled for this class</i>. If an HTTP
 * request causes an error that should always generate a stack trace, some
 * other exception type should be used instead.
 */
public class MessageHasTrailingData extends MessageException {
    /**
     * Creates new exception.
     *
     * @param message Offending message.
     */
    public MessageHasTrailingData(final Message message) {
        super(message);
    }
}
