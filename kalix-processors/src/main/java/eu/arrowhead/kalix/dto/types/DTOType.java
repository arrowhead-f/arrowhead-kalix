package eu.arrowhead.kalix.dto.types;

import eu.arrowhead.kalix.dto.Format;

import javax.lang.model.type.TypeMirror;

public interface DTOType {
    String name();

    TypeMirror asTypeMirror();

    boolean isCollection();

    boolean isReadable(Format format);

    boolean isWritable(Format format);
}
