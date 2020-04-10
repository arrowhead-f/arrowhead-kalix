package se.arkalix.security;

/**
 * Indicates that some requested operation could not be performed due to some
 * security mechanism not being enabled.
 */
public class NotSecureException extends RuntimeException {
    /**
     * @param message Description of issue.
     */
    public NotSecureException(final String message) {
        super(message);
    }
}
