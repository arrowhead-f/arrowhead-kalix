package eu.arrowhead.kalix;

import eu.arrowhead.kalix.description.ServiceDescription;

/**
 * Represents an Arrowhead Framework (AHF) service that is already provided by
 * an {@link AhfSystem}.
 * <p>
 * The interface is primarily useful because of its {@link #dismiss()} method,
 * which allows currently provided services to be dismissed.
 */
public interface AhfServiceHandle {
    /**
     * @return Description of AHF service.
     */
    ServiceDescription description();

    /**
     * Causes represented service to be dismissed, making it unavailable to
     * remote AHF systems.
     * <p>
     * Calling this method more than once has no effect.
     */
    void dismiss();

    /**
     * @return {@code true} only if this service has been dismissed, either by
     * a call to {@link #dismiss()} or by its {@link AhfSystem} being shut
     * down.
     */
    boolean isDismissed();
}
