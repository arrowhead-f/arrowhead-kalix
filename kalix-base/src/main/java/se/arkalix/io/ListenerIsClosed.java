package se.arkalix.io;

public class ListenerIsClosed extends IoException {
    public ListenerIsClosed() {
        super();
    }

    public ListenerIsClosed(final Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return "Listener is closed";
    }
}
