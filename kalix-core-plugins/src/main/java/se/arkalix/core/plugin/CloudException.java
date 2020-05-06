package se.arkalix.core.plugin;

/**
 * Signifies the failure to complete a task related to becoming or remaining a
 * member of an Arrowhead local cloud.
 * <p>
 * Examples of such tasks could be registering a provided service with the
 * service registry or getting orchestration rules.
 */
public class CloudException extends Exception {
    /**
     * Creates new cloud exception.
     *
     * @param message Description of issue.
     */
    public CloudException(final String message) {
        super(message);
    }

    /**
     * Creates new cloud exception.
     *
     * @param message Description of issue.
     * @param cause   Whatever exception caused this exception to be created.
     */
    public CloudException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
