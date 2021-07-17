package se.arkalix.util.concurrent;

public class FutureAlreadyHasConsumer extends FutureException {
    public FutureAlreadyHasConsumer() {
        super();
    }

    public FutureAlreadyHasConsumer(final String message) {
        super(message);
    }

    public FutureAlreadyHasConsumer(final Throwable cause) {
        super(cause);
    }

    public FutureAlreadyHasConsumer(final String message, final Throwable cause) {
        super(message, cause);
    }

    protected FutureAlreadyHasConsumer(
        final String message,
        final Throwable cause,
        final boolean enableSuppression,
        final boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
