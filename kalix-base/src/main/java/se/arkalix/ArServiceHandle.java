package se.arkalix;

import se.arkalix.description.ServiceDescription;
import se.arkalix.util.annotation.ThreadSafe;

/**
 * Represents an Arrowhead Framework (AHF) service that is already provided
 * by an {@link ArSystem}.
 * <p>
 * The interface is primarily useful because of its {@link #dismiss()}
 * method, which causes the service to no longer be provided when called.
 */
public interface ArServiceHandle {
    /**
     * @return Description of AHF service.
     */
    @ThreadSafe
    ServiceDescription description();

    /**
     * Causes represented service to be dismissed, making it unavailable to
     * remote AHF systems.
     * <p>
     * Calling this method more than once has no effect.
     */
    @ThreadSafe
    void dismiss();

    /**
     * @return {@code true} only if this service has been dismissed, either by
     * a call to {@link #dismiss()} or by its {@link ArSystem} being shut
     * down.
     */
    @ThreadSafe
    boolean isDismissed();
}
