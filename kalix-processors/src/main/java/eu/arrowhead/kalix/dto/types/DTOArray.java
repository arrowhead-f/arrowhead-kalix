package eu.arrowhead.kalix.dto.types;

import eu.arrowhead.kalix.dto.Format;

import javax.lang.model.type.ArrayType;

public class DTOArray implements DTOType {
    private final ArrayType type;
    private final DTOType element;

    public DTOArray(final ArrayType type, final DTOType element) {
        this.type = type;
        this.element = element;
    }

    @Override
    public ArrayType type() {
        return type;
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public boolean isReadable() {
        return element.isReadable();
    }

    public DTOType element() {
        return element;
    }

    @Override
    public boolean isReadable(final Format format) {
        return element.isReadable(format);
    }

    @Override
    public boolean isWritable() {
        return element.isWritable();
    }

    @Override
    public boolean isWritable(final Format format) {
        return element.isWritable(format);
    }
}
