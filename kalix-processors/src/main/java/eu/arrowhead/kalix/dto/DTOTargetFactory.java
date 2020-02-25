package eu.arrowhead.kalix.dto;

import eu.arrowhead.kalix.dto.types.DTOInterface;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class DTOTargetFactory {
    private final DTOPropertyFactory propertyFactory;

    public DTOTargetFactory(final Elements elementUtils, final Types typeUtils) {
        propertyFactory = new DTOPropertyFactory(elementUtils, typeUtils);
    }

    public DTOTarget createFromInterface(final TypeElement interfaceElement) throws DTOException {
        if (interfaceElement.getKind() != ElementKind.INTERFACE) {
            throw new DTOException(interfaceElement, "Only interfaces may " +
                "be annotated with @Readable and/or @Writable");
        }
        if (interfaceElement.getTypeParameters().size() != 0) {
            throw new DTOException(interfaceElement, "@Readable/@Writable " +
                "interfaces may not have type parameters");
        }
        if (interfaceElement.getInterfaces().size() != 0) {
            throw new DTOException(interfaceElement, "@Readable/@Writable " +
                "interfaces may not extend other interfaces");
        }
        if (interfaceElement.getSimpleName().toString().endsWith("DTO")) {
            throw new DTOException(interfaceElement, "@Readable/@Writable " +
                "interfaces may not have names ending with \"DTO\"");
        }

        var builder = new DTOTarget.Builder()
            .simpleName(interfaceElement.getSimpleName() + "DTO")
            .qualifiedName(interfaceElement.getQualifiedName() + "DTO");

        final var readable = interfaceElement.getAnnotation(Readable.class);
        final var writable = interfaceElement.getAnnotation(Writable.class);
        final var readableFormats = readable != null ? readable.value() : new Format[0];
        final var writableFormats = writable != null ? writable.value() : new Format[0];

        if (readableFormats.length == 0 && writableFormats.length == 0) {
            throw new DTOException(interfaceElement, "@Readable/@Writable " +
                "interfaces must have at least one readable or writable " +
                "format, specified as @Readable/@Writable annotation " +
                "arguments");
        }

        builder = builder.interfaceType(new DTOInterface(
            (DeclaredType) interfaceElement.asType(),
            readableFormats, writableFormats
        ));

        for (final var element : interfaceElement.getEnclosedElements()) {
            if (element.getEnclosingElement().getKind() != ElementKind.INTERFACE ||
                element.getKind() != ElementKind.METHOD) {
                continue;
            }
            final var modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.DEFAULT) || modifiers.contains(Modifier.STATIC)) {
                continue;
            }
            final var executable = (ExecutableElement) element;
            final var property = propertyFactory.createFromMethod(executable);
            builder = builder.addProperty(property);
        }

        return builder.build();
    }
}
