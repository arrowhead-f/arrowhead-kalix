package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.ArrayType;

public class DTOTypeArray implements DTOType {
    private final ArrayType type;
    private final DTOType elementType;

    public DTOTypeArray(final ArrayType type, final DTOType elementType) {
        this.type = type;
        this.elementType = elementType;
    }

    @Override
    public ArrayType type() {
        return type;
    }

    public DTOType elementType() {
        return elementType;
    }

    @Override
    public boolean isReadable() {
        return elementType.isReadable();
    }

    @Override
    public boolean isWritable() {
        return elementType.isWritable();
    }
}
