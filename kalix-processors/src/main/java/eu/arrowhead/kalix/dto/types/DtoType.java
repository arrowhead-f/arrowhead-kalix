package eu.arrowhead.kalix.dto.types;

import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

public interface DtoType {
    DtoDescriptor descriptor();

    TypeMirror asTypeMirror();

    default TypeName asTypeName() {
        return TypeName.get(asTypeMirror());
    }
}
