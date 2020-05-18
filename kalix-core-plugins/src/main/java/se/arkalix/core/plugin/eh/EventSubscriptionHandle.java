package se.arkalix.core.plugin.eh;

import se.arkalix.util.annotation.ThreadSafe;

/**
 * Handle useful for managing a registered {@link EventSubscription}.
 */
public interface EventSubscriptionHandle {
    /**
     * Causes represented subscription to be cancelled, if active.
     * <p>
     * Calling this method more than once has no effect.
     */
    @ThreadSafe
    void unsubscribe();
}
