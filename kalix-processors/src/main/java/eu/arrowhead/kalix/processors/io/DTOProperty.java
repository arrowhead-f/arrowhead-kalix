package eu.arrowhead.kalix.processors.io;

import eu.arrowhead.kalix.util.io.DTO;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;

public class DTOProperty {
    private final ExecutableElement origin;
    private final String defaultName;
    private final TypeMirror type;
    private final Map<DTO.Format, String> formatNames = new HashMap<>();
    private final boolean isOptional;

    public DTOProperty(final Element origin) throws DTOException {
        if (!(origin instanceof ExecutableElement) || origin.getKind() != ElementKind.METHOD) {
            throw new DTOException(origin, "Must be method");
        }
        this.origin = (ExecutableElement) origin;
        this.defaultName = origin.getSimpleName().toString();
        this.type = this.origin.getReturnType();

        final var nameJSON = origin.getAnnotation(DTO.NameJSON.class);
        if (nameJSON != null) {
            this.formatNames.put(DTO.Format.JSON, nameJSON.value());
        }

        this.isOptional = origin.getAnnotation(DTO.Optional.class) != null;
    }

    public ExecutableElement origin() {
        return origin;
    }

    public String defaultName() {
        return defaultName;
    }

    public String formatName(final DTO.Format format) {
        return formatNames.getOrDefault(format, defaultName);
    }

    public TypeMirror type() {
        return type;
    }

    public boolean isOptional() {
        return isOptional;
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
