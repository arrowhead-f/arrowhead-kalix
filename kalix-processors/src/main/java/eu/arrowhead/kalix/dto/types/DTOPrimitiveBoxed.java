package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;

public class DTOPrimitiveBoxed implements DTOType {
    private final DeclaredType type;
    private final DTODescriptor descriptor;

    public DTOPrimitiveBoxed(final DeclaredType type, final DTODescriptor descriptor) {
        this.type = type;
        this.descriptor = descriptor;
    }

    @Override
    public DTODescriptor descriptor() {
        return descriptor;
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
