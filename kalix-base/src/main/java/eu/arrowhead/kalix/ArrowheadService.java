package eu.arrowhead.kalix;

import eu.arrowhead.kalix.description.ServiceDescription;
import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;
import eu.arrowhead.kalix.descriptor.SecurityDescriptor;

import java.util.List;
import java.util.Map;

public interface ArrowheadService {
    /**
     * @return Name, or <i>service definition</i>, of this service.
     */
    String name();

    /**
     * Qualifier that distinguishes this service from other such provided by
     * the same system. How it is used depends on the application-level
     * transport protocol employed by the service.
     * <p>
     * For example, if this service uses HTTP, then the qualifier is a URL base
     * path.
     *
     * @return Service qualifier.
     */
    String qualifier();

    /**
     * @return Security schema used to authenticate and authorize service
     * users.
     */
    SecurityDescriptor security();

    /**
     * @return Metadata associated with this service. Their significance and
     * use depend on the service. The returned map should be unmodifiable.
     */
    Map<String, String> metadata();

    /**
     * @return Service version.
     */
    int version();

    /**
     * @return Interface triplets supported by the described service. The
     * returned list should be unmodifiable.
     */
    List<InterfaceDescriptor> supportedInterfaces();

    /**
     * @return Computed description of this service.
     */
    default ServiceDescription describe() {
        return new ServiceDescription.Builder()
            .name(name())
            .qualifier(qualifier())
            .security(security())
            .metadata(metadata())
            .version(version())
            .build();
    }
}
