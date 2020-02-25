package eu.arrowhead.kalix.dto.types;

import eu.arrowhead.kalix.dto.Format;

import javax.lang.model.type.PrimitiveType;

public class DTOPrimitive implements DTOType {
    private final PrimitiveType type;

    public DTOPrimitive(final PrimitiveType type) {
        this.type = type;
    }

    @Override
    public PrimitiveType type() {
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