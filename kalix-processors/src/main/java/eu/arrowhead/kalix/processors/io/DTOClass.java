package eu.arrowhead.kalix.processors.io;

import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.dto.Writable;
import eu.arrowhead.kalix.dto.Format;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a Data Transfer Object (DTO) class to be generated.
 */
public class DTOClass {
    private final TypeElement origin;
    private final String name;
    private final String qualifiedName;

    private final Set<Format> decodableFormats;
    private final Set<Format> encodableFormats;
    private final List<DTOProperty> properties = new ArrayList<>();

    public DTOClass(final Element origin, final Elements elementUtils, final Types typeUtils) throws DTOException {
        if (!(origin instanceof TypeElement) || origin.getKind() != ElementKind.INTERFACE) {
            throw new DTOException(origin, "DTO must be of type `interface`");
        }
        this.origin = (TypeElement) origin;
        if (this.origin.getTypeParameters().size() != 0) {
            throw new DTOException(origin, "DTO interfaces may not have any " +
                "type parameters");
        }
        if (this.origin.getInterfaces().size() != 0) {
            throw new DTOException(origin, "DTO interfaces may not extend " +
                "other interfaces");
        }
        if (this.origin.getSimpleName().toString().endsWith("DTO")) {
            throw new DTOException(origin, "DTO interfaces may not have " +
                "names ending with `DTO`");
        }

        this.name = origin.getSimpleName() + "DTO";
        this.qualifiedName = ((TypeElement) origin).getQualifiedName() + "DTO";
        this.decodableFormats = Optional.ofNullable(origin.getAnnotation(Readable.class))
            .map(readable -> Stream.of(readable.value()).collect(Collectors.toSet()))
            .orElse(Collections.emptySet());
        this.encodableFormats = Optional.ofNullable(origin.getAnnotation(Writable.class))
            .map(writable -> Stream.of(writable.value()).collect(Collectors.toSet()))
            .orElse(Collections.emptySet());

        if (this.decodableFormats.size() == 0 && this.encodableFormats.size() == 0) {
            throw new DTOException(origin, "DTO interface must be specified " +
                "as encodable to, or decodable from, at least one format");
        }

        for (final var element : origin.getEnclosedElements()) {
            if (element.getEnclosingElement().getKind() != ElementKind.INTERFACE ||
                element.getKind() != ElementKind.METHOD ||
                element.getModifiers().contains(Modifier.DEFAULT)
            ) {
                continue;
            }
            final var method = (ExecutableElement) element;
            if (method.getReturnType().getKind() != TypeKind.VOID &&
                method.getParameters().size() == 0 &&
                method.getTypeParameters().size() == 0
            ) {
                properties.add(new DTOProperty(element, elementUtils, typeUtils));
                continue;
            }
            throw new DTOException(element, "DTO interface methods must " +
                "either be static, provide a default implementation, or " +
                "be a simple getter, which means that is has a non-void " +
                "return type, takes no arguments and does not require any " +
                "type parameters");
        }
    }

    public TypeElement origin() {
        return origin;
    }

    public String name() {
        return name;
    }

    public String qualifiedName() {
        return qualifiedName;
    }

    public boolean isToBeDecodableAs(final Format format) {
        return decodableFormats.contains(format);
    }

    public boolean isToBeEncodableAs(final Format format) {
        return encodableFormats.contains(format);
    }

    public List<DTOProperty> properties() {
        return properties;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        DTOClass aClass = (DTOClass) o;
        return origin.getQualifiedName().equals(aClass.origin.getQualifiedName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin.getQualifiedName());
    }
}
