package eu.arrowhead.kalix.dto.types;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.Objects;

public class DtoSequence implements DtoCollection {
    private final DtoDescriptor descriptor;
    private final TypeName inputTypeName;
    private final TypeName outputTypeName;
    private final DtoType element;

    private Boolean containsInterfaceType = null;

    private DtoSequence(
        final DtoDescriptor descriptor,
        final TypeName inputTypeName,
        final TypeName outputTypeName,
        final DtoType element)
    {
        this.descriptor = descriptor;
        this.inputTypeName = inputTypeName;
        this.outputTypeName = outputTypeName;
        this.element = element;
    }

    public static DtoSequence newArray(final ArrayType type, final DtoType element) {
        Objects.requireNonNull(type, "Expected type");
        Objects.requireNonNull(type, "Expected element");
        return new DtoSequence(
            DtoDescriptor.ARRAY,
            ArrayTypeName.of(element.inputTypeName()),
            ArrayTypeName.get(type),
            element);
    }

    public static DtoSequence newList(final DeclaredType type, final DtoType element) {
        Objects.requireNonNull(type, "Expected type");
        Objects.requireNonNull(type, "Expected element");
        return new DtoSequence(
            DtoDescriptor.LIST,
            ParameterizedTypeName.get(ClassName.get(List.class), element.inputTypeName()),
            TypeName.get(type),
            element);
    }


    public DtoType element() {
        return element;
    }

    @Override
    public boolean containsInterfaceType() {
        if (containsInterfaceType == null) {
            containsInterfaceType = element.descriptor() == DtoDescriptor.INTERFACE ||
                (element instanceof DtoCollection) && ((DtoCollection) element).containsInterfaceType();
        }
        return containsInterfaceType;
    }

    @Override
    public DtoDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public TypeName inputTypeName() {
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
