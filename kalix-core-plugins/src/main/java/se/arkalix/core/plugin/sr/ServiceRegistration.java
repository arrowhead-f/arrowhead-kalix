package se.arkalix.core.plugin.sr;

import se.arkalix.security.access.AccessPolicyType;
import se.arkalix.core.plugin.SystemDetails;
import se.arkalix.ServiceRecord;
import se.arkalix.ServiceInterface;
import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.DtoJsonName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * A service registration request.
 */
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface ServiceRegistration {
    /**
     * Unique service name.
     *
     * @return Name of registered service.
     */
    @DtoJsonName("serviceDefinition")
    String name();

    /**
     * The system providing the service.
     *
     * @return Details about system providing the registered service.
     */
    @DtoJsonName("providerSystem")
    SystemDetails provider();

    /**
     * Qualifier that distinguishes this service from other such provided by
     * the same system. How it is used depends on the network protocol
     * employed by the service.
     * <p>
     * For example, if this service uses HTTP, then the qualifier is a URL base
     * path, such as {@code "/base/path"}.
     *
     * @return Service URI.
     */
    @DtoJsonName("serviceUri")
    String uri();

    /**
     * The access policy employed by the service.
     *
     * @return Service access policy.
     */
    @DtoJsonName("secure")
    Optional<AccessPolicyType> security();

    /**
     * Arbitrary metadata to associate with registered service.
     *
     * @return Service metadata map.
     */
    Map<String, String> metadata();

    /**
     * Service version.
     *
     * @return Service version.
     */
    Optional<Integer> version();

    /**
     * Interface triplets supported by the service.
     * <p>
     * If the service is provided securely over HTTP and supports JSON and XML,
     * then its two triplets would be "HTTP-SECURE-JSON" and "HTTP-SECURE-XML".
     *
     * @return List of supported service interface triplets.
     */
    List<ServiceInterface> interfaces();

    /**
     * Creates a new service registration from given service record.
     *
     * @param description Service record from which to create registration.
     * @return Created service registration.
     */
    static ServiceRegistrationDto from(final ServiceRecord description) {
        return new ServiceRegistrationDto.Builder()
            .name(description.name())
            .provider(SystemDetails.from(description.provider()))
            .uri(description.uri())
            .security(description.accessPolicyType())
            .metadata(description.metadata())
            .version(description.version())
            .interfaces(new ArrayList<>(description.interfaces()))
            .build();
    }
}
