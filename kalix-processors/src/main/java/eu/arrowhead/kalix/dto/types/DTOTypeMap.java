package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;

public class DTOTypeMap implements DTOType {
    private final DeclaredType type;
    private final DTOType keyType;
    private final DTOType valueType;

    public DTOTypeMap(final DeclaredType type, final DTOType keyType, final DTOType valueType) {
        this.type = type;
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public DeclaredType type() {
        return type;
    }

    public DTOType keyType() {
        return keyType;
    }

    public DTOType valueType() {
        return valueType;
    }

    @Override
    public boolean isReadable() {
        return keyType.isReadable() && valueType.isReadable();
    }

    @Override
    public boolean isWritable() {
        return keyType.isWritable() && valueType.isWritable();
    }
}