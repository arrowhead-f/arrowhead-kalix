package eu.arrowhead.kalix.dto;

import eu.arrowhead.kalix.dto.types.DtoInterface;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;

public class DtoTargetFactory {
    private final DtoPropertyFactory propertyFactory;

    public DtoTargetFactory(final Elements elementUtils, final Types typeUtils) {
        propertyFactory = new DtoPropertyFactory(elementUtils, typeUtils);
    }

    public DtoTarget createFromInterface(final TypeElement interfaceElement) throws DtoException {
        if (interfaceElement.getKind() != ElementKind.INTERFACE) {
            throw new DtoException(interfaceElement, "Only interfaces may " +
                "be annotated with @Readable and/or @Writable");
        }
        if (interfaceElement.getTypeParameters().size() != 0) {
            throw new DtoException(interfaceElement, "@Readable/@Writable " +
                "interfaces may not have type parameters");
        }
        if (interfaceElement.getInterfaces().size() != 0) {
            throw new DtoException(interfaceElement, "@Readable/@Writable " +
                "interfaces may not extend other interfaces");
        }
        if (interfaceElement.getSimpleName().toString().endsWith(DtoTarget.NAME_SUFFIX)) {
            throw new DtoException(interfaceElement, "@Readable/@Writable " +
                "interfaces may not have names ending with \"" +
                DtoTarget.NAME_SUFFIX + "\"");
        }

        final var readable = interfaceElement.getAnnotation(Readable.class);
        final var writable = interfaceElement.getAnnotation(Writable.class);
        final var readableEncodings = readable != null ? readable.value() : new DataEncoding[0];
        final var writableEncodings = writable != null ? writable.value() : new DataEncoding[0];

        if (readableEncodings.length == 0 && writableEncodings.length == 0) {
            throw new DtoException(interfaceElement, "@Readable/@Writable " +
                "interfaces must have at least one readable or writable " +
                "encodings, specified as @Readable/@Writable annotation " +
                "arguments");
        }

        final var declaredType = (DeclaredType) interfaceElement.asType();
        final var interfaceType = new DtoInterface(declaredType, readableEncodings, writableEncodings);

        final var properties = new ArrayList<DtoProperty>();
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
            properties.add(property);
        }

        return new DtoTarget(interfaceType, properties);
    }
}
