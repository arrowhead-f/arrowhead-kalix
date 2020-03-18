package eu.arrowhead.kalix.core.plugins.srv.dto;

import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;
import eu.arrowhead.kalix.descriptor.SecurityDescriptor;
import eu.arrowhead.kalix.dto.Writable;
import eu.arrowhead.kalix.dto.json.JsonName;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A service registration request.
 */
@Writable
public interface ServiceRegistration {
    /**
     * Unique service name.
     */
    @JsonName("serviceDefinition")
    String name();

    /**
     * The system providing the service.
     */
    @JsonName("providerSystem")
    SystemDefinition provider();

    /**
     * Service qualifier.
     * <p>
     * If the application-level transport protocol is HTTP, then the qualifier
     * is a URL base path, such as "/base-path".
     */
    @JsonName("serviceUri")
    String qualifier();

    /**
     * The security mode of the service.
     */
    @JsonName("secure")
    Optional<SecurityDescriptor> security();

    /**
     * Arbitrary metadata to associate with registered service.
     */
    Map<String, String> metadata();

    /**
     * Service version.
     */
    Optional<Integer> version();

    /**
     * Interface triplets supported by the service.
     * <p>
     * If the service is provided securely over HTTP and supports JSON and XML,
     * then its two triplets would be "HTTP-SECURE-JSON" and "HTTP-SECURE-XML".
     */
    @JsonName("interfaces")
    List<InterfaceDescriptor> supportedInterfaces();
}
