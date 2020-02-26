package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;

public class DTOMap implements DTOType {
    private final DeclaredType type;
    private final DTOType key;
    private final DTOType value;

    public DTOMap(final DeclaredType type, final DTOType key, final DTOType value) {
        assert !key.descriptor().isCollection() && !(key instanceof DTOInterface);

        this.type = type;
        this.key = key;
        this.value = value;
    }

    public DTOType key() {
        return key;
    }

    public DTOType value() {
        return value;
    }

    @Override
    public DTODescriptor descriptor() {
        return DTODescriptor.MAP;
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