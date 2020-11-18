package se.arkalix.dto.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.DeclaredType;
import java.util.Map;

public class DtoMap implements DtoCollection {
    private final ParameterizedTypeName inputTypeName;
    private final TypeName outputTypeName;
    private final DtoType key;
    private final DtoType value;

    private Boolean containsInterfaceType = null;

    public DtoMap(final DeclaredType type, final DtoType key, final DtoType value) {
        assert !key.descriptor().isCollection() && !(key instanceof DtoInterface);

        this.inputTypeName = ParameterizedTypeName.get(
            ClassName.get(Map.class),
            key.inputTypeName(),
            value.inputTypeName());
        this.outputTypeName = TypeName.get(type);
        this.key = key;
        this.value = value;
    }

    public DtoType key() {
        return key;
    }

    public DtoType value() {
        return value;
    }

    @Override
    public boolean containsInterfaceType() {
        if (containsInterfaceType == null) {
            containsInterfaceType = value.descriptor() == DtoDescriptor.INTERFACE ||
                (value instanceof DtoCollection) && ((DtoCollection) value).containsInterfaceType();
        }
        return containsInterfaceType;
    }

    @Override
    public DtoDescriptor descriptor() {
        return DtoDescriptor.MAP;
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
        return "Map<" + key + ", " + value + ">";
    }
}