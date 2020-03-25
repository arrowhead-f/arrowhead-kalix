package se.arkalix.security.access;

public class AccessTokenException extends Exception {
    public AccessTokenException(final String message) {
        super(message, null, true, false);
    }

    public AccessTokenException(final String message, final Throwable cause) {
        super(message, cause, true, false);
    }
}
