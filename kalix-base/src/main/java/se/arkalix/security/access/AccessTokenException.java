package se.arkalix.security.access;

/**
 * Indicates that something is preventing a given access token from being used
 * to authenticate some consumer.
 */
public class AccessTokenException extends Exception {
    /**
     * @param message Description of token issue.
     */
    public AccessTokenException(final String message) {
        super(message, null, true, false);
    }

    /**
     * @param message Description of token issue.
     * @param cause   Exception causing the issue in question.
     */
    public AccessTokenException(final String message, final Throwable cause) {
        super(message, cause, true, false);
    }
}
