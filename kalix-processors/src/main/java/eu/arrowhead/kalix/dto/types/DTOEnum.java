package eu.arrowhead.kalix.dto.types;

import eu.arrowhead.kalix.dto.Format;

import javax.lang.model.type.DeclaredType;

public class DTOEnum implements DTOType {
    private final DeclaredType type;

    public DTOEnum(final DeclaredType type) {
        this.type = type;
    }

    @Override
    public String name() {
        return type.asElement().getSimpleName().toString();
    }

    @Override
    public DeclaredType asTypeMirror() {
        return type;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public boolean isReadable(final Format format) {
        return true;
    }

    @Override
    public boolean isWritable(final Format format) {
        return true;
    }
}
