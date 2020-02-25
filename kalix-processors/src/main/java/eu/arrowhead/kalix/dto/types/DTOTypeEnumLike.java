package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class DTOTypeEnumLike implements DTOType {
    private final DeclaredType type;
    private final boolean isReadable;
    private final boolean isWritable;

    public DTOTypeEnumLike(final DeclaredType type, final boolean isReadable, final boolean isWritable) {
        this.type = type;
        this.isReadable = isReadable;
        this.isWritable = isWritable;
    }

    @Override
    public TypeMirror type() {
        return type;
    }

    @Override
    public boolean isReadable() {
        return isReadable;
    }

    @Override
    public boolean isWritable() {
        return isWritable;
    }
}
