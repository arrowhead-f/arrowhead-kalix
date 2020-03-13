package eu.arrowhead.kalix.plugin;

import eu.arrowhead.kalix.ArrowheadSystem;

/**
 * Represents a {@link Plugin}'s attachment to its {@link ArrowheadSystem}.
 * <p>
 * Most significantly, it can be used to detach the {@link Plugin}, causing it
 * to no longer receive events from the {@link ArrowheadSystem} it is attached
 * to.
 */
public interface Plug {
    /**
     * Detaches a plugin from its {@link ArrowheadSystem}, causing it to no
     * longer receive events from it.
     */
    void detach();

    /**
     * @return The system the {@link Plugin} is attached to.
     */
    ArrowheadSystem system();

    /**
     * @return {@code true} only if the {@link ArrowheadSystem} this plug is
     * attached to is about to shut down irreversibly. This happens typically
     * because the scheduler used by the system is being shut down.
     * @see eu.arrowhead.kalix.util.concurrent.FutureScheduler FutureScheduler
     */
    boolean isSystemShuttingDown();
}
