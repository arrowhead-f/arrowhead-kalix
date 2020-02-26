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
    public String name() {
        return element.name() + "[]";
    }

    @Override
    public ArrayType asTypeMirror() {
        return type;
    }
}
