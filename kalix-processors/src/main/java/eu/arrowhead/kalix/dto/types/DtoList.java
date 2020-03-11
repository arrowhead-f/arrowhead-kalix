package eu.arrowhead.kalix.dto.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.DeclaredType;
import java.util.List;

public class DtoList implements DtoArrayOrList {
    private final ParameterizedTypeName inputTypeName;
    private final TypeName outputTypeName;
    private final DtoType element;

    public DtoList(final DeclaredType type, final DtoType element) {
        this.inputTypeName = ParameterizedTypeName.get(ClassName.get(List.class), element.inputTypeName());
        this.outputTypeName = TypeName.get(type);
        this.element = element;
    }

    @Override
    public DtoType element() {
        return element;
    }

    @Override
    public DtoDescriptor descriptor() {
        return DtoDescriptor.LIST;
    }

    @Override
    public ParameterizedTypeName inputTypeName() {
        return inputTypeName;
    }

    @Override
    public TypeName outputTypeName() {
        return outputTypeName;
    }

    @Override
    public String toString() {
        return "List<" + element + ">";
    }
}