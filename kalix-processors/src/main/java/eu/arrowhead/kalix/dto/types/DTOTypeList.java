package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;

public class DTOTypeList implements DTOType {
    private final DeclaredType type;
    private final DTOType elementType;

    public DTOTypeList(final DeclaredType type, final DTOType elementType) {
        this.type = type;
        this.elementType = elementType;
    }

    @Override
    public DeclaredType type() {
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