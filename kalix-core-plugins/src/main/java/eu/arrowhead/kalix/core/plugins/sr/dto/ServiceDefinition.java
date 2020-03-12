package eu.arrowhead.kalix.core.plugins.sr.dto;

import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.dto.json.JsonName;

@Readable
public interface ServiceDefinition {
    long id();

    @JsonName("serviceDefinition")
    String name();

    String createdAt();

    String updatedAt();
}
