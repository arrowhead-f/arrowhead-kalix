package eu.arrowhead.kalix.dto.types;

import eu.arrowhead.kalix.dto.Format;

import javax.lang.model.type.DeclaredType;

public class DTOEnumLike implements DTOType {
    private final DeclaredType type;

    public DTOEnumLike(final DeclaredType type) {
        this.type = type;
    }

    @Override
    public DeclaredType type() {
        return type;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isReadable(final Format format) {
        return true;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isWritable(final Format format) {
        return true;
    }
}
