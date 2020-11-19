package se.arkalix.dto.types;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.Objects;

public class DtoTypeSequence implements DtoTypeCollection {
    private static final ClassName listClassName = ClassName.get(List.class);

    private final DtoDescriptor descriptor;
    private final TypeName interfaceTypeName;
    private final TypeName generatedTypeName;
    private final DtoType itemType;

    private Boolean containsInterfaceType = null;
    private Boolean containsOptional = null;

    private DtoTypeSequence(
        final DtoDescriptor descriptor,
        final TypeName interfaceTypeName,
        final TypeName generatedTypeName,
        final DtoType itemType
    )
    {
        this.descriptor = descriptor;
        this.interfaceTypeName = interfaceTypeName;
        this.generatedTypeName = generatedTypeName;
        this.itemType = itemType;
    }

    public static DtoTypeSequence newArray(final ArrayType type, final DtoType itemType) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(itemType, "itemType");
        return new DtoTypeSequence(
            DtoDescriptor.ARRAY,
            ArrayTypeName.get(type),
            ArrayTypeName.of(itemType.generatedTypeName()),
            itemType
        );
    }

    public static DtoTypeSequence newList(final DeclaredType type, final DtoType itemType) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(itemType, "itemType");
        return new DtoTypeSequence(
            DtoDescriptor.LIST,
            TypeName.get(type),
            ParameterizedTypeName.get(listClassName, itemType.generatedTypeName()),
            itemType
        );
    }

    public DtoType itemType() {
        return itemType;
    }

    @Override
    public boolean containsInterfaceType() {
        if (containsInterfaceType == null) {
            containsInterfaceType = itemType.descriptor() == DtoDescriptor.INTERFACE ||
                (itemType instanceof DtoTypeCollection) && ((DtoTypeCollection) itemType).containsInterfaceType();
        }
        return containsInterfaceType;
    }

    @Override
    public boolean containsOptional() {
        if (containsOptional == null) {
            containsOptional = itemType.descriptor() == DtoDescriptor.OPTIONAL ||
                (itemType instanceof DtoTypeCollection) && ((DtoTypeCollection) itemType).containsOptional();
        }
        return containsOptional;
    }

    @Override
    public DtoDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public TypeName interfaceTypeName() {
        return interfaceTypeName;
    }

    @Override
    public TypeName generatedTypeName() {
        return generatedTypeName;
    }

    @Override
    public String toString() {
        return itemType + "[]";
    }
}
