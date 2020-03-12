package eu.arrowhead.kalix.core.plugins.sr.dto;

import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;
import eu.arrowhead.kalix.descriptor.SecurityDescriptor;
import eu.arrowhead.kalix.dto.Writable;
import eu.arrowhead.kalix.dto.json.JsonName;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Writable
public interface ServiceRecordQuery {
    @JsonName("serviceDefinitionRequirement")
    String serviceName();

    @JsonName("interfaceRequirements")
    List<InterfaceDescriptor> acceptableInterfaces();

    @JsonName("securityRequirements")
    List<SecurityDescriptor> acceptableSecurityModes();

    @JsonName("metadataRequirements")
    Map<String, String> metadata();

    @JsonName("versionRequirement")
    Optional<Integer> version();

    @JsonName("maxVersionRequirement")
    Optional<Integer> versionMax();

    @JsonName("minVersionRequirement")
    Optional<Integer> versionMin();

    @JsonName("pingProviders")
    Optional<Boolean> triggerPing();
}
