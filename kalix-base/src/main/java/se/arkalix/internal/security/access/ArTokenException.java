package se.arkalix.internal.security.access;

public class ArTokenException extends Exception {
    public ArTokenException(final String message) {
        super(message, null, true, false);
    }

    public ArTokenException(final String message, final Throwable cause) {
        super(message, cause, true, false);
    }
}
