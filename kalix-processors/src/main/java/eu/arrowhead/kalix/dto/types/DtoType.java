package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.TypeMirror;

public interface DtoType {
    DtoDescriptor descriptor();

    TypeMirror asTypeMirror();
}
