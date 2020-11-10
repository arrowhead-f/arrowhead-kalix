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

    public DtoTarget createFromInterface(final TypeElement interfaceElement) {
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
        final var readableCodecs = readable != null ? readable.value() : new DtoCodec[0];
        final var writableCodecs = writable != null ? writable.value() : new DtoCodec[0];
        Arrays.sort(readableCodecs);
        Arrays.sort(writableCodecs);

        if (readableCodecs.length == 0 && writableCodecs.length == 0) {
            throw new DtoException(interfaceElement, "@DtoReadableAs/@DtoWritableAs " +
                "interfaces must have at least one readable or writable " +
                "codecs, specified as @DtoReadableAs/@DtoWritableAs annotation " +
                "arguments");
        }

        final var declaredType = (DeclaredType) interfaceElement.asType();
        final var interfaceType = new DtoInterface(declaredType, readableCodecs, writableCodecs);

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
            verifyAnyExclusivityConstraints(readableCodecs, writableCodecs, executable);
            final var property = propertyFactory.createFromMethod(executable);
            properties.add(property);
        }

        final var isComparable = interfaceElement.getAnnotation(DtoEqualsHashCode.class) != null;
        final var isPrintable = interfaceElement.getAnnotation(DtoToString.class) != null;

        return new DtoTarget(interfaceType, properties, isComparable, isPrintable);
    }

    private void verifyAnyExclusivityConstraints(
        final DtoCodec[] readable,
        final DtoCodec[] writable,
        final ExecutableElement executable
    ) {
        final var type = executable.getReturnType();
        if (type.getKind() != TypeKind.DECLARED) {
            return;
        }
        final var exclusive = ((DeclaredType) type).asElement().getAnnotation(DtoExclusive.class);
        if (exclusive == null) {
            return;
        }
        final var exclusiveCodec = exclusive.value();
        final var isExclusiveCodecReadable = Arrays.binarySearch(readable, exclusiveCodec) >= 0;
        if (!isExclusiveCodecReadable || readable.length != 1) {
            throw new DtoException(executable, "The type " + type + " is " +
                "annotated with @DtoExclusive(" + exclusiveCodec + ") " +
                "while the DTO interface " + executable.getEnclosingElement() +
                " is annotated with @DtoReadableAs(" + Stream.of(readable)
                .map(String::valueOf).collect(Collectors.joining(", ")) +
                "); " + exclusiveCodec + " exclusivity constraint violated");
        }
        final var isExclusiveCodecWritable = Arrays.binarySearch(writable, exclusiveCodec) >= 0;
        if (!isExclusiveCodecWritable || writable.length != 1) {
            throw new DtoException(executable, "The type " + type + " is " +
                "annotated with @DtoExclusive(" + exclusiveCodec + ") " +
                "while the DTO interface " + executable.getEnclosingElement() +
                " is annotated with @DtoWritableAs(" + Stream.of(writable)
                .map(String::valueOf).collect(Collectors.joining(", ")) +
                "); " + exclusiveCodec + " exclusivity constraint violated");
        }
    }
}
