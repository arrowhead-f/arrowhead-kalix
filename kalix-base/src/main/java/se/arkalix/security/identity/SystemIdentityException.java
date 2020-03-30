package se.arkalix.security.identity;

public class SystemIdentityException extends Exception {
    public SystemIdentityException(final String message) {
        super(message);
    }

    public SystemIdentityException(final String message, final IllegalStateException cause) {
        super(message, cause);
    }
}
