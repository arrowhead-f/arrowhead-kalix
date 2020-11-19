package se.arkalix.dto.types;

import com.squareup.javapoet.TypeName;

public interface DtoType {
    DtoDescriptor descriptor();

    TypeName interfaceTypeName();

    default TypeName generatedTypeName() {
        return interfaceTypeName();
    }
}
