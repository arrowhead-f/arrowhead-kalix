package se.arkalix.io;

public class SocketIsClosed extends IoException {
    public SocketIsClosed() {
        super();
    }

    public SocketIsClosed(final String message) {
        super(message);
    }

    public SocketIsClosed(final Throwable cause) {
        super(cause);
    }

    public SocketIsClosed(final String message, final Throwable cause) {
        super(message, cause);
    }

    protected SocketIsClosed(
        final String message,
        final Throwable cause,
        final boolean enableSuppression,
        final boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
