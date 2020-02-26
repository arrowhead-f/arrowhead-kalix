package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.TypeMirror;

public interface DTOType {
    String typeName();

    TypeMirror asTypeMirror();

    boolean isCollection();
}
