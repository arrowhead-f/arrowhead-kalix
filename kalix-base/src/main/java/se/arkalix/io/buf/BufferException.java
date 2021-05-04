package se.arkalix.io.buf;

import se.arkalix.io.IoException;

public class BufferException extends IoException {
    public BufferException() {
        super();
    }

    public BufferException(final String message) {
        super(message);
    }

    public BufferException(final Throwable cause) {
        super(cause);
    }

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
