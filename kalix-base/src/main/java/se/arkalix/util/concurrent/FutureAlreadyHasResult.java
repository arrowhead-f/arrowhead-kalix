package se.arkalix.util.concurrent;

public class FutureAlreadyHasResult extends FutureException {
    public FutureAlreadyHasResult() {
        super();
    }

    public FutureAlreadyHasResult(final String message) {
        super(message);
    }

    public FutureAlreadyHasResult(final Throwable cause) {
        super(cause);
    }

    public FutureAlreadyHasResult(final String message, final Throwable cause) {
        super(message, cause);
    }

    protected FutureAlreadyHasResult(
        final String message,
        final Throwable cause,
        final boolean enableSuppression,
        final boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
