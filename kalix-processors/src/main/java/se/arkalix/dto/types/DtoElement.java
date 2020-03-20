package se.arkalix.dto.types;

import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

public class DtoElement implements DtoType {
    private final TypeName typeName;
    private final DtoDescriptor descriptor;

    public DtoElement(final TypeMirror type, final DtoDescriptor descriptor) {
        this.typeName = TypeName.get(type);
        this.descriptor = descriptor;
    }

    @Override
    public DtoDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public TypeName inputTypeName() {
        return typeName;
    }

    @Override
    public TypeName outputTypeName() {
        return typeName;
    }

    @Override
    public String toString() {
        return typeName.toString();
    }
}
