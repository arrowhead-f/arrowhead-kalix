package se.arkalix.util.concurrent;

public class FutureException extends RuntimeException {
    public FutureException() {
        super();
    }

    public FutureException(final String message) {
        super(message);
    }

    public FutureException(final Throwable cause) {
        super(cause);
    }

    public FutureException(final String message, final Throwable cause) {
        super(message, cause);
    }

    protected FutureException(
        final String message,
        final Throwable cause,
        final boolean enableSuppression,
        final boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
