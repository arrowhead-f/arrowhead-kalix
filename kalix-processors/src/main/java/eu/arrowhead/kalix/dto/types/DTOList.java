package eu.arrowhead.kalix.dto.types;

import eu.arrowhead.kalix.dto.Format;

import javax.lang.model.type.DeclaredType;

public class DTOList implements DTOType {
    private final DeclaredType type;
    private final DTOType element;

    public DTOList(final DeclaredType type, final DTOType element) {
        this.type = type;
        this.element = element;
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