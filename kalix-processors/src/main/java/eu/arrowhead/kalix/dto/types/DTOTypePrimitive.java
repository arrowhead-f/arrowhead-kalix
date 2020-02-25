package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.PrimitiveType;

public class DTOTypePrimitive implements DTOType {
    private final PrimitiveType type;

    public DTOTypePrimitive(final PrimitiveType type) {
        this.type = type;
    }

    @Override
    public PrimitiveType type() {
        return type;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isWritable() {
        return true;
    }
}