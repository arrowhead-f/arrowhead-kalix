package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.TypeMirror;

public interface DTOType {
    DTODescriptor descriptor();

    TypeMirror asTypeMirror();
}
