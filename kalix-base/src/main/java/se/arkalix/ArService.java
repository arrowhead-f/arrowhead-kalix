package se.arkalix;

import se.arkalix.description.ServiceDescription;

/**
 * An Arrowhead Framework (AHF) service that can be provided by an
 * {@link ArSystem}.
 */
public interface ArService {
    /**
     * Creates a description of this service.
     *
     * @param system The system owning this service.
     * @return New description.
     */
    ServiceDescription describeAsProvidedBy(ArSystem system);
}
