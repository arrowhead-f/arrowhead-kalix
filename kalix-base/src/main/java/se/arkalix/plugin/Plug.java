package se.arkalix.plugin;

import se.arkalix.ArSystem;

/**
 * Represents a {@link Plugin}'s attachment to its {@link ArSystem}.
 * <p>
 * Most significantly, it can be used to detach the {@link Plugin}, causing it
 * to no longer receive events from the {@link ArSystem} it is attached
 * to.
 */
public interface Plug {
    /**
     * Detaches a plugin from its {@link ArSystem}, causing it to no
     * longer receive events from it.
     */
    void detach();

    /**
     * @return The system the {@link Plugin} is attached to.
     */
    ArSystem system();

    /**
     * @return {@code true} only if the {@link ArSystem} this plug is
     * attached to is about to shut down irreversibly.
     */
    default boolean isSystemShuttingDown() {
        return system().isShuttingDown();
    }
}
