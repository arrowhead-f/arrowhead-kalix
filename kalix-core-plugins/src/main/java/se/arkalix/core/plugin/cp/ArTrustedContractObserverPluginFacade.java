package se.arkalix.core.plugin.cp;

import se.arkalix.plugin.PluginFacade;

/**
 * {@link PluginFacade Facade} allowing for contract negotiations to be
 * observed.
 */
@SuppressWarnings("unused")
public interface ArTrustedContractObserverPluginFacade extends PluginFacade {
    /**
     * Registers observer to receive contract negotiation updates.
     *
     * @param observer Observer to register.
     */
    void observe(final TrustedContractObserverHandler observer);
}
