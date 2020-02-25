package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.TypeMirror;

public interface DTOType {
    TypeMirror type();

    boolean isReadable();

    boolean isWritable();
}
