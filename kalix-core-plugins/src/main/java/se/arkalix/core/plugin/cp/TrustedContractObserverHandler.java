package se.arkalix.core.plugin.cp;

/**
 * A receiver of contract negotiation updates.
 */
@FunctionalInterface
public interface TrustedContractObserverHandler {
    /**
     * Method to be invoked whenever a new contract negotiation update is
     * observed.
     *
     * @param session Updated negotiation session.
     */
    void onUpdate(final TrustedContractNegotiationDto session);
}
