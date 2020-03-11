package eu.arrowhead.kalix.core.plugins.sr.dto;

import eu.arrowhead.kalix.descriptor.SecurityDescriptor;
import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.dto.json.JsonName;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Readable
public interface ServiceRecord {
    long id();

    @JsonName("serviceDefinition")
    ServiceDefinition definition();

    SystemDefinition provider();

    @JsonName("serviceUri")
    String basePath();

    @JsonName("endOfValidity")
    Instant expiresAt();

    @JsonName("secure")
    SecurityDescriptor security();

    Map<String, String> metadata();

    int version();

    @JsonName("interfaces")
    List<InterfaceDefinition> supportedInterfaces();

    Instant createdAt();

    Instant updatedAt();
}
