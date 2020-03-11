package eu.arrowhead.kalix.core.plugins.sr.dto;

import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.dto.Writable;
import eu.arrowhead.kalix.dto.json.JsonName;

import java.time.Instant;

@Readable
@Writable
public interface InterfaceDefinition {
    long id();

    @JsonName("interfaceName")
    String name();

    Instant createdAt();

    Instant updatedAt();
}
