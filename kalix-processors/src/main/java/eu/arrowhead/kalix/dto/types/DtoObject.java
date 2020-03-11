package eu.arrowhead.kalix.dto.types;

import com.squareup.javapoet.TypeName;

import javax.lang.model.type.DeclaredType;

public class DtoObject implements DtoType {
    private final TypeName typeName;
    private final DtoDescriptor descriptor;

    public DtoObject(final DeclaredType type, final DtoDescriptor descriptor) {
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
