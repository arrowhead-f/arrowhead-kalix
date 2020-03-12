package eu.arrowhead.kalix.plugin;

/**
 * An {@link eu.arrowhead.kalix.ArrowheadSystem ArrowheadSystem} plugin.
 */
public interface Plugin {
    void onAttach(final Contact contact);

    void onServiceProvided();

    void onDetach();

    /**
     * Represents a plugin's attachment to its
     * {@link eu.arrowhead.kalix.ArrowheadSystem ArrowheadSystem}.
     * <p>
     * Most significantly, it can be used to detach a plugin, causing to no
     * longer receive events from the {@link eu.arrowhead.kalix.ArrowheadSystem ArrowheadSystem}
     * it is attached to.
     */
    @FunctionalInterface
    interface Contact {
        void detach();
    }
}
