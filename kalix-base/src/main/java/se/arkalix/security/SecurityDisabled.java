package se.arkalix.security;

/**
 * Indicates that some requested operation could not be performed due to some
 * security mechanism not being enabled.
 */
public class SecurityDisabled extends SecurityException {
    /**
     * @param message Description of issue.
     */
    public SecurityDisabled(final String message) {
        super(message);
    }
}
