package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;

public class DtoEnum implements DtoType {
    private final DeclaredType type;

    public DtoEnum(final DeclaredType type) {
        this.type = type;
    }

    @Override
    public DtoDescriptor descriptor() {
        return DtoDescriptor.ENUM;
    }

    @Override
    public DeclaredType asTypeMirror() {
        return type;
    }

    @Override
    public String toString() {
        return type.asElement().getSimpleName().toString();
    }
}
