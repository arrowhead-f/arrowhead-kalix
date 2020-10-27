package se.arkalix.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the failure to execute some DTO related operation.
 * <p>
 * As this kind of exceptions are expected to predominantly be thrown due to
 * external rather than internal causes, <i>they do not produce stack traces
 * unless debug logging is enabled for this class</i>.
 *
 * @see se.arkalix.dto
 */
@SuppressWarnings("unused")
public class DtoException extends Exception {
    private static final Logger logger = LoggerFactory.getLogger(DtoException.class);

    /**
     * Creates new DTO exception.
     */
    public DtoException() {
        this(null, null);
    }

    /**
     * Creates new DTO exception.
     *
     * @param message Describes cause of exception being thrown.
     */
    public DtoException(final String message) {
        this(message, null);
    }

    /**
     * Creates new DTO exception.
     *
     * @param message Describes cause of exception being thrown.
     * @param cause   Caught exception leading to this exception being thrown.
     */
    public DtoException(final String message, final Throwable cause) {
        super(message, cause, true, logger.isDebugEnabled());
    }

    /**
     * Creates new DTO exception.
     *
     * @param cause   Caught exception leading to this exception being thrown.
     */
    public DtoException(final Throwable cause) {
        this(null, cause);
    }
}
