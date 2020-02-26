package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;

public class DTOEnum implements DTOType {
    private final DeclaredType type;

    public DTOEnum(final DeclaredType type) {
        this.type = type;
    }

    @Override
    public String typeName() {
        return type.asElement().getSimpleName().toString();
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
