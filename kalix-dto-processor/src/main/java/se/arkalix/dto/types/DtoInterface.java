package se.arkalix.dto.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import se.arkalix.dto.*;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DtoInterface implements DtoType {
    private final Set<DtoCodecSpec> readableCodecs;
    private final Set<DtoCodecSpec> writableCodecs;
    private final Set<DtoCodecSpec> codecs;

    private final Elements elementUtils;
    private final TypeElement interfaceElement;
    private final DeclaredType interfaceType;
    private final String simpleName;
    private final String dataSimpleName;
    private final String builderSimpleName;
    private final TypeName inputTypeName;
    private final TypeName outputTypeName;

    public DtoInterface(final Elements elementUtils, final Element element) {
        if (element.getKind() != ElementKind.INTERFACE) {
            throw new DtoException(element, "Only interfaces may " +
                "be annotated with @DtoReadableAs and/or @DtoWritableAs");
        }
        final var interfaceElement = (TypeElement) element;

        this.elementUtils = Objects.requireNonNull(elementUtils, "elementUtils");
        this.interfaceElement = Objects.requireNonNull(interfaceElement, "interfaceElement");

        if (this.interfaceElement.getTypeParameters().size() != 0) {
            throw new DtoException(interfaceElement, "@DtoReadableAs/@DtoWritableAs " +
                "interfaces may not have type parameters");
        }
        if (this.interfaceElement.getSimpleName().toString().endsWith(DtoTarget.DATA_SUFFIX)) {
            throw new DtoException(interfaceElement, "@DtoReadableAs/@DtoWritableAs " +
                "interfaces may not have names ending with \"" +
                DtoTarget.DATA_SUFFIX + "\"");
        }

        interfaceType = (DeclaredType) interfaceElement.asType();

        readableCodecs = collectReadableCodecs(interfaceElement);
        writableCodecs = collectWritableCodecs(interfaceElement);
        codecs = Stream.concat(this.readableCodecs.stream(), this.writableCodecs.stream())
            .collect(Collectors.toUnmodifiableSet());

        simpleName = interfaceElement.getSimpleName().toString();
        dataSimpleName = simpleName + DtoTarget.DATA_SUFFIX;
        builderSimpleName = simpleName + DtoTarget.BUILDER_SUFFIX;

        final var packageName = elementUtils.getPackageOf(interfaceElement);
        inputTypeName = ClassName.get(packageName.toString(), dataSimpleName);
        outputTypeName = TypeName.get(interfaceType);
    }

    private static Set<DtoCodecSpec> collectReadableCodecs(final TypeElement interfaceElement) {
        final var annotation = interfaceElement.getAnnotation(DtoReadableAs.class);
        if (annotation == null) {
            return Collections.emptySet();
        }
        final var values = annotation.value();
        if (values.length == 0) {
            throw new DtoException(interfaceElement, "@DtoReadableAs " +
                "must be provided with at least one DtoCodec value.");
        }
        Arrays.sort(values);
        return codecSpecsFrom(values, interfaceElement);
    }

    private static Set<DtoCodecSpec> collectWritableCodecs(final TypeElement interfaceElement) {
        final var annotation = interfaceElement.getAnnotation(DtoWritableAs.class);
        if (annotation == null) {
            return Collections.emptySet();
        }
        final var values = annotation.value();
        if (values.length == 0) {
            throw new DtoException(interfaceElement, "@DtoWritableAs " +
                "must be provided with at least one DtoCodec value.");
        }
        Arrays.sort(values);
        return codecSpecsFrom(values, interfaceElement);
    }

    private static Set<DtoCodecSpec> codecSpecsFrom(
        final DtoCodec[] dtoCodecs,
        final TypeElement interfaceElement
    ) {
        return Arrays.stream(dtoCodecs)
            .map(dtoName -> DtoCodecSpec.getByDtoCodec(dtoName)
                .orElseThrow(() -> new DtoException(interfaceElement, "" +
                    "No DtoImplementer available for codec \"" + dtoName
                    + "\"; cannot generate DTO class for " + interfaceElement.getSimpleName())))
            .collect(Collectors.toUnmodifiableSet());
    }

    public Stream<ExecutableElement> collectPropertyMethods() {
        return elementUtils.getAllMembers(interfaceElement)
            .stream()
            .filter(member -> {
                if (member.getEnclosingElement().getKind() != ElementKind.INTERFACE ||
                    member.getKind() != ElementKind.METHOD)
                {
                    return false;
                }
                final var modifiers = member.getModifiers();
                return !modifiers.contains(Modifier.DEFAULT) && !modifiers.contains(Modifier.STATIC);
            })
            .map(member -> (ExecutableElement) member);
    }

    public DeclaredType type() {
        return interfaceType;
    }

    public String simpleName() {
        return simpleName;
    }

    public String dataSimpleName() {
        return dataSimpleName;
    }

    public String builderSimpleName() {
        return builderSimpleName;
    }

    public Set<DtoCodecSpec> codecs() {
        return codecs;
    }

    public Set<DtoCodecSpec> readableCodecs() {
        return readableCodecs;
    }

    public Set<DtoCodecSpec> writableCodecs() {
        return writableCodecs;
    }

    @Override
    public DtoDescriptor descriptor() {
        return DtoDescriptor.INTERFACE;
    }

    @Override
    public TypeName inputTypeName() {
        return inputTypeName;
    }

    @Override
    public TypeName outputTypeName() {
        return outputTypeName;
    }

    public boolean isAnnotatedWith(final Class<? extends Annotation> annotationClass) {
        return interfaceElement.getAnnotation(annotationClass) != null;
    }

    public boolean isReadableAs(final DtoCodecSpec dtoCodec) {
        return readableCodecs.contains(dtoCodec);
    }

    public boolean isWritableAs(final DtoCodecSpec dtoCodec) {
        return writableCodecs.contains(dtoCodec);
    }

    @Override
    public String toString() {
        return simpleName;
    }
}
