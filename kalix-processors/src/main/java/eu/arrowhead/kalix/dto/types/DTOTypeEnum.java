package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.TypeMirror;

public class DTOTypeEnum implements DTOType {
    private final TypeMirror type;

    public DTOTypeEnum(final TypeMirror type) {
        this.type = type;
    }

    @Override
    public TypeMirror type() {
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
