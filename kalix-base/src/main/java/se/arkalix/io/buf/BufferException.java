package se.arkalix.io.buf;

import se.arkalix.io.IoException;

/**
 * Signifies that an operation on a {@link Buffer} failed.
 */
public class BufferException extends IoException {
    /**
     * Creates new exception.
     */
    public BufferException() {
        super();
    }

    /**
     * Creates new exception.
     *
     * @param message Description to associate with exception.
     */
    public BufferException(final String message) {
        super(message);
    }

    /**
     * Creates new exception.
     *
     * @param cause Exception causing this exception to be thrown.
     */
    public BufferException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates new exception.
     *
     * @param message Description to associate with exception.
     * @param cause   Exception causing this exception to be thrown.
     */
    public BufferException(final String message, final Throwable cause) {
        super(message, cause);
    }

    protected BufferException(
        final String message,
        final Throwable cause,
        final boolean enableSuppression,
        final boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
