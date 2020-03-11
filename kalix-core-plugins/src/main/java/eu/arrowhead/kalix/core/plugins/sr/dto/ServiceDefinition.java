package eu.arrowhead.kalix.core.plugins.sr.dto;

import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.dto.json.JsonName;

import java.time.Instant;

@Readable
public interface ServiceDefinition {
    long id();

    @JsonName("serviceDefinition")
    String name();

    Instant createdAt();

    Instant updatedAt();
}
