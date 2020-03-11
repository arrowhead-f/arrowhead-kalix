package eu.arrowhead.kalix.dto.types;

import com.squareup.javapoet.TypeName;

import javax.lang.model.type.ArrayType;

public class DtoArray implements DtoArrayOrList {
    private final ArrayType type;
    private final DtoType element;

    public DtoArray(final ArrayType type, final DtoType element) {
        this.type = type;
        this.element = element;
    }

    @Override
    public DtoType element() {
        return element;
    }

    @Override
    public DtoDescriptor descriptor() {
        return DtoDescriptor.ARRAY;
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
