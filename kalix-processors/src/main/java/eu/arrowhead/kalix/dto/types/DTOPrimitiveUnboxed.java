package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.PrimitiveType;

public class DTOPrimitiveUnboxed implements DTOType {
    private final PrimitiveType type;
    private final DTODescriptor descriptor;

    public DTOPrimitiveUnboxed(final PrimitiveType type, final DTODescriptor descriptor) {
        this.type = type;
        this.descriptor = descriptor;
    }

    @Override
    public DTODescriptor descriptor() {
        return descriptor;
    }

    @Override
    public PrimitiveType asTypeMirror() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}