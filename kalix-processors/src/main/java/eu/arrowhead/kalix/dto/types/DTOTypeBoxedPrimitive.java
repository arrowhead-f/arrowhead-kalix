package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;

public class DTOTypeBoxedPrimitive implements DTOType {
    private final DeclaredType type;

    public DTOTypeBoxedPrimitive(final DeclaredType type) {
        this.type = type;
    }

    @Override
    public DeclaredType type() {
        return type;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isWritable() {
        return true;
    }
}
