package eu.arrowhead.kalix;

import eu.arrowhead.kalix.description.ServiceDescriptor;

/**
 * An abstract Arrowhead Framework (AHF) service that can be provided by an
 * {@link ArrowheadSystem}.
 */
public interface Service {
    /**
     * @return Description of service.
     */
    ServiceDescriptor descriptor();
}
