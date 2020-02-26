package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;

public class DTOString implements DTOType {
    private final DeclaredType type;

    public DTOString(final DeclaredType type) {
        this.type = type;
    }

    @Override
    public String typeName() {
        return "String";
    }

    @Override
    public DeclaredType asTypeMirror() {
        return type;
    }

    @Override
    public boolean isCollection() {
        return false;
    }
}
