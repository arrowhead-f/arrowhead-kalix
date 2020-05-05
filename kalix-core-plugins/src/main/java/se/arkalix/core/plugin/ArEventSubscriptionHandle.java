package se.arkalix.core.plugin;

import se.arkalix.util.annotation.ThreadSafe;

/**
 * Handle useful for managing a registered {@link ArEventSubscription}.
 */
public interface ArEventSubscriptionHandle {
    /**
     * Causes represented subscription to be cancelled, if active.
     * <p>
     * Calling this method more than once has no effect.
     */
    @ThreadSafe
    void unsubscribe();
}
