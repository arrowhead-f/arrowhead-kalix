package se.arkalix.core.plugin.sr;

import se.arkalix.core.plugin.SystemDetails;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.descriptor.InterfaceDescriptor;
import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.JsonName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * A service registration request.
 */
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
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
    SystemDetails provider();

    /**
     * Qualifier that distinguishes this service from other such provided by
     * the same system. How it is used depends on the application-level
     * transport protocol employed by the service.
     * <p>
     * For example, if this service uses HTTP, then the qualifier is a URL base
     * path, such as {@code "/base/path"}.
     */
    @JsonName("serviceUri")
    String uri();

    /**
     * The access policy employed by the service.
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
    List<InterfaceDescriptor> interfaces();

    static ServiceRegistrationDto from(final ServiceDescription description) {
        return new ServiceRegistrationBuilder()
            .name(description.name())
            .provider(SystemDetails.from(description.provider()))
            .uri(description.uri())
            .security(description.security())
            .metadata(description.metadata())
            .version(description.version())
            .interfaces(new ArrayList<>(description.interfaces()))
            .build();
    }
}
