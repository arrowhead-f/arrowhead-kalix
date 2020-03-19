package eu.arrowhead.kalix;

import eu.arrowhead.kalix.description.ServiceDescription;

/**
 * An Arrowhead Framework (AHF) service that can be provided by an
 * {@link AhfSystem}.
 */
public interface AhfService {
    /**
     * Creates a description of this service.
     *
     * @param system The system owning this service.
     * @return New description.
     */
    ServiceDescription describeAsProvidedBy(AhfSystem system);
}
