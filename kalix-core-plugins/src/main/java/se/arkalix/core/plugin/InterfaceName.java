package se.arkalix.core.plugin;

import se.arkalix.descriptor.InterfaceDescriptor;
import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.json.JsonName;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * The name of an {@link InterfaceDescriptor interface triplet}.
 */
@DtoReadableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface InterfaceName {
    /**
     * Interface triplet.
     */
    @JsonName("interfaceName")
    InterfaceDescriptor name();
}
