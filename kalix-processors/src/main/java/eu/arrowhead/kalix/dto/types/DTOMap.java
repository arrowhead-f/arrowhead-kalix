package eu.arrowhead.kalix.dto.types;

import eu.arrowhead.kalix.dto.Format;

import javax.lang.model.type.DeclaredType;

public class DTOMap implements DTOType {
    private final DeclaredType type;
    private final DTOType key;
    private final DTOType value;

    public DTOMap(final DeclaredType type, final DTOType key, final DTOType value) {
        assert key.isReadable() && key.isWritable() &&
            !key.isCollection() && !(key instanceof DTOInterface);

        this.type = type;
        this.key = key;
        this.value = value;
    }

    @Override
    public DeclaredType type() {
        return type;
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public boolean isReadable() {
        return value.isReadable();
    }

    public DTOType key() {
        return key;
    }

    public DTOType value() {
        return value;
    }

    @Override
    public boolean isReadable(final Format format) {
        return value.isReadable(format);
    }

    @Override
    public boolean isWritable() {
        return value.isWritable();
    }

    @Override
    public boolean isWritable(final Format format) {
        return value.isWritable(format);
    }
}