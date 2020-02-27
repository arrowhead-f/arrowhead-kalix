package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;

public class DtoMap implements DtoType {
    private final DeclaredType type;
    private final DtoType key;
    private final DtoType value;

    public DtoMap(final DeclaredType type, final DtoType key, final DtoType value) {
        assert !key.descriptor().isCollection() && !(key instanceof DtoInterface);

        this.type = type;
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
    public DtoDescriptor descriptor() {
        return DtoDescriptor.MAP;
    }

    @Override
    public DeclaredType asTypeMirror() {
        return type;
    }

    @Override
    public String toString() {
        return "Map<" + key + ", " + value + ">";
    }
}