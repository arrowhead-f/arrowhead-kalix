package eu.arrowhead.kalix.core.plugins.srv.dto;

import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;
import eu.arrowhead.kalix.descriptor.SecurityDescriptor;
import eu.arrowhead.kalix.dto.Writable;
import eu.arrowhead.kalix.dto.json.JsonName;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Writable
public interface ServiceRecordForm {
    @JsonName("serviceDefinition")
    String name();

    @JsonName("providerSystem")
    SystemDefinitionForm provider();

    @JsonName("serviceUri")
    String qualifier();

    @JsonName("endOfValidity")
    Optional<String> expiresAt();

    @JsonName("secure")
    Optional<SecurityDescriptor> security();

    Map<String, String> metadata();

    Optional<Integer> version();

    @JsonName("interfaces")
    List<InterfaceDescriptor> supportedInterfaces();
}
