package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;

public class DtoString implements DtoType {
    private final DeclaredType type;

    public DtoString(final DeclaredType type) {
        this.type = type;
    }

    @Override
    public DtoDescriptor descriptor() {
        return DtoDescriptor.STRING;
    }

    @Override
    public DeclaredType asTypeMirror() {
        return type;
    }

    @Override
    public String toString() {
        return "String";
    }
}
