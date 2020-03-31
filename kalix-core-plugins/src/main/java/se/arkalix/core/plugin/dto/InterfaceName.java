package se.arkalix.core.plugin.dto;

import se.arkalix.descriptor.InterfaceDescriptor;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.json.JsonName;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * The name of an {@link InterfaceDescriptor interface triplet}.
 */
@DtoReadableAs(JSON)
public interface InterfaceName {
    /**
     * Interface triplet.
     */
    @JsonName("interfaceName")
    InterfaceDescriptor name();
}
