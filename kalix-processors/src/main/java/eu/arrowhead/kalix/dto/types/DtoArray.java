package eu.arrowhead.kalix.dto.types;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.ArrayType;

public class DtoArray implements DtoArrayOrList {
    private final ArrayTypeName inputTypeName;
    private final ArrayTypeName outputTypeName;
    private final DtoType element;

    public DtoArray(final ArrayType type, final DtoType element) {
        this.inputTypeName = ArrayTypeName.of(element.inputTypeName());
        this.outputTypeName = ArrayTypeName.get(type);
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
    public ArrayTypeName inputTypeName() {
        return inputTypeName;
    }

    @Override
    public TypeName outputTypeName() {
        return outputTypeName;
    }

    @Override
    public String toString() {
        return element + "[]";
    }
}
