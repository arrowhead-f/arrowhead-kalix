package eu.arrowhead.kalix;

import java.util.List;

/**
 * An abstract Arrowhead Framework (AHF) service.
 */
public interface ArrowheadService {
    /**
     * @return The name, also known as the <i>service definition</i>, of this
     * service.
     */
    String getName();

    /**
     * @return A list of all interface triples supported by this service.
     */
    List<InterfaceTriple> getSupportedInterfaces();
}
