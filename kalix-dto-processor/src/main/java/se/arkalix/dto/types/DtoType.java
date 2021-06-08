package se.arkalix.dto.types;

import com.squareup.javapoet.TypeName;

public interface DtoType {
    DtoDescriptor descriptor();

    /**
     * Gets type name as it appears in the DTO interface it was found in.
     *
     * @return Type name as expressed in original DTO interface.
     */
    TypeName originalTypeName();

    /**
     * Gets type name where any DTO interface types are substituted with their
     * generated implementations.
     *
     * @return Type name containing generated DTO classes, or the original type
     * name if not applicable.
     */
    default TypeName generatedTypeName() {
        return originalTypeName();
    }
}
