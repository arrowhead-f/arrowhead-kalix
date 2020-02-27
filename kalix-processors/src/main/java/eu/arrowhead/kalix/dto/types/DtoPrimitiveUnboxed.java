package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.PrimitiveType;

public class DtoPrimitiveUnboxed implements DtoType {
    private final PrimitiveType type;
    private final DtoDescriptor descriptor;

    public DtoPrimitiveUnboxed(final PrimitiveType type, final DtoDescriptor descriptor) {
        this.type = type;
        this.descriptor = descriptor;
    }

    @Override
    public DtoDescriptor descriptor() {
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