package se.arkalix.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Signifies that some operation on a message failed.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces unless debug-level logging is enabled for this class</i>. If an HTTP
 * request causes an error that should always generate a stack trace, some
 * other exception type should be used instead.
 */
@SuppressWarnings("unused")
public class MessageException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(MessageException.class);

    private final Message message;

    /**
     * Creates new exception.
     *
     * @param message Offending message.
     */
    public MessageException(final Message message) {
        this(message, null, null);
    }

    /**
     * Creates new exception.
     *
     * @param message     Offending message.
     * @param description Description to associate with exception.
     */
    public MessageException(final Message message, final String description) {
        this(message, description, null);
    }

    /**
     * Creates new exception.
     *
     * @param message     Offending message.
     * @param description Description to associate with exception.
     * @param cause       Exception causing this exception to be thrown.
     */
    public MessageException(final Message message, final String description, final Throwable cause) {
        super(description, cause, true, logger.isDebugEnabled());
        this.message = Objects.requireNonNull(message);
    }

    /**
     * Creates new exception.
     *
     * @param message Offending message.
     * @param cause   Exception causing this exception to be thrown.
     */
    public MessageException(final Message message, final Throwable cause) {
        this(message, null, cause);
    }

    /**
     * Gets {@link Message} causing this exception to be thrown.
     *
     * @return Offending message.
     */
    public Message message() {
        return message;
    }
}
