package eu.arrowhead.kalix.processors.io;

import eu.arrowhead.kalix.dto.Format;
import eu.arrowhead.kalix.dto.NameJSON;
import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.dto.Writable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DTOProperty {
    private final ExecutableElement origin;
    private final String defaultName;
    private final Map<Format, String> formatNames = new HashMap<>();
    private final TypeMirror typeMirror;
    private final boolean isOptional;
    private final boolean isReadable;
    private final boolean isWritable;

    public DTOProperty(final Element origin, final Elements elementUtils, final Types typeUtils) throws DTOException {
        if (!(origin instanceof ExecutableElement) || origin.getKind() != ElementKind.METHOD) {
            throw new DTOException(origin, "Must be method");
        }
        this.origin = (ExecutableElement) origin;
        this.defaultName = origin.getSimpleName().toString();

        final var nameJSON = origin.getAnnotation(NameJSON.class);
        if (nameJSON != null) {
            this.formatNames.put(Format.JSON, nameJSON.value());
        }

        var typeMirror = this.origin.getReturnType();

        var isOptional = false;
        if (typeMirror instanceof DeclaredType && ((DeclaredType) typeMirror).getTypeArguments().size() == 1) {
            final var type0 = (DeclaredType) typeMirror;
            final var inner = type0.getTypeArguments().get(0);
            final var optional = typeUtils.getDeclaredType(
                elementUtils.getTypeElement(Optional.class.getCanonicalName()), inner);
            if (typeUtils.isAssignable(typeMirror, optional)) {
                typeMirror = type0.getTypeArguments().get(0);
                isOptional = true;
            }
        }

        var isReadable = true;
        var isWritable = true;
        if (!typeMirror.getKind().isPrimitive()) {
            final var typeElement = typeUtils.asElement(typeMirror);
            isReadable = typeElement.getAnnotation(Readable.class) != null;
            isWritable = typeElement.getAnnotation(Writable.class) != null;
        }

        this.typeMirror = typeMirror;
        this.isOptional = isOptional;
        this.isReadable = isReadable;
        this.isWritable = isWritable;
    }

    public ExecutableElement origin() {
        return origin;
    }

    public String defaultName() {
        return defaultName;
    }

    public String formatName(final Format format) {
        return formatNames.getOrDefault(format, defaultName);
    }

    public TypeMirror typeMirror() {
        return typeMirror;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public boolean isReadable() {
        return isReadable;
    }

    public boolean isWritable() {
        return isWritable;
    }

    @Override
    public int hashCode() {
        return defaultName.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final DTOProperty property = (DTOProperty) o;
        return defaultName.equals(property.defaultName);
    }
}
