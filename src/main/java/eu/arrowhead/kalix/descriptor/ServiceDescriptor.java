package eu.arrowhead.kalix.descriptor;

import java.util.Collections;
import java.util.List;

/**
 * Describes an Arrowhead Framework service.
 */
public class ServiceDescriptor {
    private final String name;
    private final List<InterfaceDescriptor> supportedInterfaces;

    /**
     * Creates new Arrowhead Framework service descriptor.
     *
     * @param name                Name, or <i>service definition</i>, of service.
     * @param supportedInterfaces Interface triplets supported by service.
     */
    public ServiceDescriptor(final String name, final List<InterfaceDescriptor> supportedInterfaces) {
        this.name = name;
        this.supportedInterfaces = Collections.unmodifiableList(supportedInterfaces);
    }

    /**
     * @return Name, or <i>service definition</i>, of service.
     */
    public String name() {
        return name;
    }

    /**
     * Note that the returned collection is read-only.
     *
     * @return Interface triplets supported by service.
     */
    public List<InterfaceDescriptor> supportedInterfaces() {
        return supportedInterfaces;
    }
}
