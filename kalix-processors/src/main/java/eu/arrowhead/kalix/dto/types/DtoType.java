package eu.arrowhead.kalix.dto.types;

import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

public interface DtoType {
    DtoDescriptor descriptor();

    TypeName inputTypeName();

    TypeName outputTypeName();
}
