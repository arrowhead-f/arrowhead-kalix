package eu.arrowhead.kalix.dto.types;

import eu.arrowhead.kalix.dto.Format;

import javax.lang.model.type.TypeMirror;

public interface DTOType {
    TypeMirror type();

    boolean isCollection();

    boolean isReadable();

    boolean isReadable(Format format);

    boolean isWritable();

    boolean isWritable(Format format);
}
