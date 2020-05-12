package se.arkalix.core.plugin.cp;

import se.arkalix.plugin.PluginFacade;

public interface ArContractObservationTrustedPluginFacade extends PluginFacade {
    void observe(final ArTrustedNegotiationObserver observer);
}
