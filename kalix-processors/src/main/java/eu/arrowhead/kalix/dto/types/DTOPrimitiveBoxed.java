package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;

public class DTOPrimitiveBoxed implements DTOPrimitive {
    private final DeclaredType type;
    private final DTOPrimitiveType primitiveType;

    public DTOPrimitiveBoxed(final DeclaredType type, final DTOPrimitiveType primitiveType) {
        this.type = type;
        this.primitiveType = primitiveType;
    }

    @Override
    public DTOPrimitiveType primitiveType() {
        return primitiveType;
    }

    @Override
    public String typeName() {
        return type.asElement().getSimpleName().toString();
    }

    @Override
    public DeclaredType asTypeMirror() {
        return type;
    }
}
