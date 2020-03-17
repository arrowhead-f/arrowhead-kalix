package eu.arrowhead.kalix.plugin;

import eu.arrowhead.kalix.AhfSystem;

/**
 * Represents a {@link Plugin}'s attachment to its {@link AhfSystem}.
 * <p>
 * Most significantly, it can be used to detach the {@link Plugin}, causing it
 * to no longer receive events from the {@link AhfSystem} it is attached
 * to.
 */
public interface Plug {
    /**
     * Detaches a plugin from its {@link AhfSystem}, causing it to no
     * longer receive events from it.
     */
    void detach();

    /**
     * @return The system the {@link Plugin} is attached to.
     */
    AhfSystem system();

    /**
     * @return {@code true} only if the {@link AhfSystem} this plug is
     * attached to is about to shut down irreversibly.
     */
    boolean isSystemShuttingDown();
}
