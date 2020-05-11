package se.arkalix.dto;

import se.arkalix.dto.types.DtoInterface;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DtoTargetFactory {
    private final Elements elementUtils;
    private final DtoPropertyFactory propertyFactory;

    public DtoTargetFactory(final Elements elementUtils, final Types typeUtils) {
        this.elementUtils = elementUtils;

        propertyFactory = new DtoPropertyFactory(elementUtils, typeUtils);
    }

    public DtoTarget createFromInterface(final TypeElement interfaceElement) throws DtoException {
        if (interfaceElement.getKind() != ElementKind.INTERFACE) {
            throw new DtoException(interfaceElement, "Only interfaces may " +
                "be annotated with @DtoReadableAs and/or @DtoWritableAs");
        }
        if (interfaceElement.getTypeParameters().size() != 0) {
            throw new DtoException(interfaceElement, "@DtoReadableAs/@DtoWritableAs " +
                "interfaces may not have type parameters");
        }
        if (interfaceElement.getSimpleName().toString().endsWith(DtoTarget.DATA_SUFFIX)) {
            throw new DtoException(interfaceElement, "@DtoReadableAs/@DtoWritableAs " +
                "interfaces may not have names ending with \"" +
                DtoTarget.DATA_SUFFIX + "\"");
        }

        final var readable = interfaceElement.getAnnotation(DtoReadableAs.class);
        final var writable = interfaceElement.getAnnotation(DtoWritableAs.class);
        final var readableEncodings = readable != null ? readable.value() : new DtoEncoding[0];
        final var writableEncodings = writable != null ? writable.value() : new DtoEncoding[0];
        Arrays.sort(readableEncodings);
        Arrays.sort(writableEncodings);

        if (readableEncodings.length == 0 && writableEncodings.length == 0) {
            throw new DtoException(interfaceElement, "@DtoReadableAs/@DtoWritableAs " +
                "interfaces must have at least one readable or writable " +
                "encodings, specified as @DtoReadableAs/@DtoWritableAs annotation " +
                "arguments");
        }

        final var declaredType = (DeclaredType) interfaceElement.asType();
        final var interfaceType = new DtoInterface(declaredType, readableEncodings, writableEncodings);

        final var properties = new ArrayList<DtoProperty>();
        for (final var element : elementUtils.getAllMembers(interfaceElement)) {
            if (element.getEnclosingElement().getKind() != ElementKind.INTERFACE ||
                element.getKind() != ElementKind.METHOD)
            {
                continue;
            }
            final var modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.DEFAULT) || modifiers.contains(Modifier.STATIC)) {
                continue;
            }
            final var executable = (ExecutableElement) element;
            verifyAnyExclusivityConstraints(readableEncodings, writableEncodings, executable);
            final var property = propertyFactory.createFromMethod(executable);
            properties.add(property);
        }

        final var isComparable = interfaceElement.getAnnotation(DtoEqualsHashCode.class) != null;
        final var isPrintable = interfaceElement.getAnnotation(DtoToString.class) != null;

        return new DtoTarget(interfaceType, properties, isComparable, isPrintable);
    }

    private void verifyAnyExclusivityConstraints(
        final DtoEncoding[] readable,
        final DtoEncoding[] writable,
        final ExecutableElement executable) throws DtoException
    {
        final var type = executable.getReturnType();
        if (type.getKind() != TypeKind.DECLARED) {
            return;
        }
        final var exclusive = ((DeclaredType) type).asElement().getAnnotation(DtoExclusive.class);
        if (exclusive == null) {
            return;
        }
        final var exclusiveEncoding = exclusive.value();
        final var isExclusiveEncodingReadable = Arrays.binarySearch(readable, exclusiveEncoding) >= 0;
        if (!isExclusiveEncodingReadable || readable.length != 1) {
            throw new DtoException(executable, "The type " + type + " is " +
                "annotated with @DtoExclusive(" + exclusiveEncoding + ") " +
                "while the DTO interface " + executable.getEnclosingElement() +
                " is annotated with @DtoReadableAs(" + Stream.of(readable)
                .map(String::valueOf).collect(Collectors.joining(", ")) +
                "); " + exclusiveEncoding + " exclusivity constraint violated");
        }
        final var isExclusiveEncodingWritable = Arrays.binarySearch(writable, exclusiveEncoding) >= 0;
        if (!isExclusiveEncodingWritable || writable.length != 1) {
            throw new DtoException(executable, "The type " + type + " is " +
                "annotated with @DtoExclusive(" + exclusiveEncoding + ") " +
                "while the DTO interface " + executable.getEnclosingElement() +
                " is annotated with @DtoWritableAs(" + Stream.of(writable)
                .map(String::valueOf).collect(Collectors.joining(", ")) +
                "); " + exclusiveEncoding + " exclusivity constraint violated");
        }
    }
}
