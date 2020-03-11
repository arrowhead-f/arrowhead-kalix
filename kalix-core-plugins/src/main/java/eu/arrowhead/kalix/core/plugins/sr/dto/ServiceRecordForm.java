package eu.arrowhead.kalix.core.plugins.sr.dto;

import eu.arrowhead.kalix.descriptor.SecurityDescriptor;
import eu.arrowhead.kalix.dto.Writable;
import eu.arrowhead.kalix.dto.json.JsonName;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Writable
public interface ServiceRecordForm {
    @JsonName("serviceDefinition")
    String serviceName();

    @JsonName("providerSystem")
    SystemDefinitionForm provider();

    @JsonName("serviceUri")
    String basePath();

    @JsonName("endOfValidity")
    Optional<Instant> expiresAt();

    @JsonName("secure")
    Optional<SecurityDescriptor> security();

    Optional<Map<String, String>> metadata();

    Optional<Integer> version();

    @JsonName("interfaces")
    List<InterfaceDefinition> supportedInterfaces();
}
