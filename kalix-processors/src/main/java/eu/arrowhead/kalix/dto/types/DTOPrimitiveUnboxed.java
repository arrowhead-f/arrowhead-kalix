package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.PrimitiveType;

public class DTOPrimitiveUnboxed implements DTOPrimitive {
    private final PrimitiveType type;
    private final DTOPrimitiveType primitiveType;

    public DTOPrimitiveUnboxed(final PrimitiveType type, final DTOPrimitiveType primitiveType) {
        this.type = type;
        this.primitiveType = primitiveType;
    }

    @Override
    public DTOPrimitiveType primitiveType() {
        return primitiveType;
    }

    @Override
    public String name() {
        return type.toString();
    }

    @Override
    public PrimitiveType asTypeMirror() {
        return type;
    }
}