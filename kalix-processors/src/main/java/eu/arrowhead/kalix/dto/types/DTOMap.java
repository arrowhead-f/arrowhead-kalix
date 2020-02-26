package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;

public class DTOMap implements DTOType {
    private final DeclaredType type;
    private final DTOType key;
    private final DTOType value;

    public DTOMap(final DeclaredType type, final DTOType key, final DTOType value) {
        assert !key.isCollection() && !(key instanceof DTOInterface);

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
    public String typeName() {
        return "Map<" + key.typeName() + ", " + value.typeName() + ">";
    }

    @Override
    public DeclaredType asTypeMirror() {
        return type;
    }

    @Override
    public boolean isCollection() {
        return true;
    }
}