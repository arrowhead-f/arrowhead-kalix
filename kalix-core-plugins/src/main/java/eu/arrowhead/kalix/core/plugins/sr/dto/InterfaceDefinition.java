package eu.arrowhead.kalix.core.plugins.sr.dto;

import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;
import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.dto.Writable;
import eu.arrowhead.kalix.dto.json.JsonName;

@Readable
@Writable
public interface InterfaceDefinition {
    long id();

    @JsonName("interfaceName")
    InterfaceDescriptor name();

    String createdAt();

    String updatedAt();
}
