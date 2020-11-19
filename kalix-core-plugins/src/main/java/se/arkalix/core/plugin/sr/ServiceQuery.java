package se.arkalix.core.plugin.sr;

import se.arkalix.security.access.AccessPolicyType;
import se.arkalix.ServiceInterface;
import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.DtoJsonName;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * A search query for service definitions.
 */
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface ServiceQuery {
    /**
     * Service name. <b>Must be specified.</b>
     */
    @DtoJsonName("serviceDefinitionRequirement")
    String name();

    /**
     * Supported service interfaces.
     */
    @DtoJsonName("interfaceRequirements")
    List<ServiceInterface> interfaces();

    /**
     * Supported access policy types.
     */
    @DtoJsonName("securityRequirements")
    List<AccessPolicyType> accessPolicyTypes();

    /**
     * Required metadata pairs.
     */
    @DtoJsonName("metadataRequirements")
    Map<String, String> metadata();

    /**
     * Desired exact service version.
     */
    @DtoJsonName("versionRequirement")
    Optional<Integer> version();

    /**
     * The highest permitted service version.
     */
    @DtoJsonName("maxVersionRequirement")
    Optional<Integer> versionMax();

    /**
     * The lowest permitted service version.
     */
    @DtoJsonName("minVersionRequirement")
    Optional<Integer> versionMin();

    /**
     * Whether or not to test if matching services are live before returning
     * {@link ServiceQueryResult}.
     */
    @DtoJsonName("pingProviders")
    Optional<Boolean> triggerPing();

    static ServiceQueryDto from(final se.arkalix.query.ServiceQuery query) {
        final var isSecure = query.isSecure();
        return new ServiceQueryDto.Builder()
            .name(query.name().orElse(null))
            .interfaces(query.protocolTypes()
                .stream()
                .flatMap(protocolType -> query.codecTypes()
                    .stream()
                    .map(codec -> ServiceInterface.getOrCreate(protocolType, isSecure, codec)))
                .collect(Collectors.toUnmodifiableList()))
            .metadata(query.metadata())
            .version(query.version().orElse(null))
            .versionMax(query.versionMax().orElse(null))
            .versionMin(query.versionMin().orElse(null))
            .build();
    }
}
