package se.arkalix.security;

/**
 * Indicates that some operation failed due to some security requirement not
 * being satisfied.
 */
public class SecurityException extends RuntimeException {
    /**
     * Creates new security exception.
     *
     * @param message Description of issue.
     */
    public SecurityException(final String message) {
        super(message);
    }

    /**
     * Creates new security exception.
     *
     * @param message Description of issue.
     * @param cause   Cause of issue.
     */
    public SecurityException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
