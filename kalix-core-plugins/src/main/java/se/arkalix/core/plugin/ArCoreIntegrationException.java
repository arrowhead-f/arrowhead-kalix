package se.arkalix.core.plugin;

/**
 * Signifies the failure to complete a core integration task.
 * <p>
 * Examples of such tasks could be registering a provided service with the
 * service registry or getting orchestration rules.
 */
public class ArCoreIntegrationException extends Exception {
    /**
     * Creates new core integration exception.
     *
     * @param message Description of issue.
     */
    public ArCoreIntegrationException(final String message) {
        super(message);
    }

    /**
     * Creates new core integration exception.
     *
     * @param message Description of issue.
     * @param cause   Whatever exception caused this exception to be created.
     */
    public ArCoreIntegrationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
