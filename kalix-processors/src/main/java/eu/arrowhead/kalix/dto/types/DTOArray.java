package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.ArrayType;

public class DTOArray implements DTOArrayOrList {
    private final ArrayType type;
    private final DTOType element;

    public DTOArray(final ArrayType type, final DTOType element) {
        this.type = type;
        this.element = element;
    }

    @Override
    public DTOType element() {
        return element;
    }

    @Override
    public DTODescriptor descriptor() {
        return DTODescriptor.ARRAY;
    }

    @Override
    public ArrayType asTypeMirror() {
        return type;
    }

    @Override
    public String toString() {
        return element + "[]";
    }
}
