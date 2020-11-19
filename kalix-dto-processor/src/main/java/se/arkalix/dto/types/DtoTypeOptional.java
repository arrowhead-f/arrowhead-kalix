package se.arkalix.dto.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.DeclaredType;
import java.util.Objects;
import java.util.Optional;

public class DtoTypeOptional implements DtoTypeCollection {
    private static final ClassName optionalClassName = ClassName.get(Optional.class);

    private final TypeName interfaceTypeName;
    private final ParameterizedTypeName generatedClassName;
    private final DtoType valueType;

    private Boolean containsInterfaceType = null;
    private Boolean containsOptional = null;

    public DtoTypeOptional(final DeclaredType type, final DtoType valueType) {
        Objects.requireNonNull(type, "type");
        this.valueType = Objects.requireNonNull(valueType, "valueType");

        interfaceTypeName = ClassName.get(type);
        generatedClassName = ParameterizedTypeName.get(optionalClassName, valueType.generatedTypeName());
    }

    public DtoType valueType() {
        return valueType;
    }

    @Override
    public boolean containsInterfaceType() {
        if (containsInterfaceType == null) {
            containsInterfaceType = valueType.descriptor() == DtoDescriptor.INTERFACE ||
                (valueType instanceof DtoTypeCollection) && ((DtoTypeCollection) valueType).containsInterfaceType();
        }
        return containsInterfaceType;
    }

    @Override
    public boolean containsOptional() {
        if (containsOptional == null) {
            containsOptional = valueType.descriptor() == DtoDescriptor.OPTIONAL ||
                (valueType instanceof DtoTypeCollection) && ((DtoTypeCollection) valueType).containsOptional();
        }
        return containsOptional;
    }

    @Override
    public DtoDescriptor descriptor() {
        return DtoDescriptor.OPTIONAL;
    }

    @Override
    public TypeName interfaceTypeName() {
        return interfaceTypeName;
    }

    @Override
    public ParameterizedTypeName generatedTypeName() {
        return generatedClassName;
    }
}
