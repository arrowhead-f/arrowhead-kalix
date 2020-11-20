package se.arkalix.dto.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.DeclaredType;
import java.util.Map;

public class DtoTypeMap implements DtoTypeCollection {
    private final TypeName originalTypeName;
    private final ParameterizedTypeName generatedTypeName;
    private final DtoType keyType;
    private final DtoType valueType;

    private Boolean containsInterfaceType = null;
    private Boolean containsOptional = null;

    public DtoTypeMap(final DeclaredType type, final DtoType keyType, final DtoType valueType) {
        assert !keyType.descriptor().isCollection() && !(keyType instanceof DtoTypeInterface);

        this.originalTypeName = TypeName.get(type);
        this.generatedTypeName = ParameterizedTypeName.get(
            ClassName.get(Map.class),
            keyType.generatedTypeName(),
            valueType.generatedTypeName());
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public DtoType keyType() {
        return keyType;
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
        return DtoDescriptor.MAP;
    }

    @Override
    public TypeName originalTypeName() {
        return originalTypeName;
    }

    @Override
    public ParameterizedTypeName generatedTypeName() {
        return generatedTypeName;
    }

    @Override
    public String toString() {
        return "Map<" + keyType + ", " + valueType + ">";
    }
}