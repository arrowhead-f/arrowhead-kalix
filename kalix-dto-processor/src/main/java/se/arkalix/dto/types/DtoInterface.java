package se.arkalix.dto.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import se.arkalix.dto.DtoCodec;
import se.arkalix.dto.DtoCodecSpec;
import se.arkalix.dto.DtoException;
import se.arkalix.dto.DtoTarget;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.*;
import java.util.stream.Collectors;

public class DtoInterface implements DtoType {
    private final Set<DtoCodecSpec> readableCodecs;
    private final Set<DtoCodecSpec> writableCodecs;
    private final Set<DtoCodecSpec> dtoCodecs;

    private final DeclaredType interfaceType;
    private final String simpleName;
    private final String dataSimpleName;
    private final String builderSimpleName;
    private final TypeName inputTypeName;
    private final TypeName outputTypeName;

    public DtoInterface(
        final DeclaredType interfaceType,
        final DtoCodec[] readableCodecs,
        final DtoCodec[] writableCodecs
    ) {
        this.interfaceType = Objects.requireNonNull(interfaceType, "interfaceType");
        this.readableCodecs = codecSpecsFrom(readableCodecs, interfaceType);
        this.writableCodecs = codecSpecsFrom(writableCodecs, interfaceType);

        dtoCodecs = new HashSet<>();
        dtoCodecs.addAll(this.readableCodecs);
        dtoCodecs.addAll(this.writableCodecs);

        final TypeElement interfaceElement = (TypeElement) interfaceType.asElement();
        simpleName = interfaceElement.getSimpleName().toString();
        dataSimpleName = simpleName + DtoTarget.DATA_SUFFIX;
        builderSimpleName = simpleName + DtoTarget.BUILDER_SUFFIX;
        inputTypeName = ClassName.get(packageNameOf(interfaceElement.getQualifiedName()), dataSimpleName);
        outputTypeName = TypeName.get(interfaceType);
    }

    private static Set<DtoCodecSpec> codecSpecsFrom(final DtoCodec[] dtoCodecs, final DeclaredType interfaceType) {
        if (dtoCodecs == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(dtoCodecs)
            .map(dtoName -> DtoCodecSpec.getByDtoCodec(dtoName)
                .orElseThrow(() -> new DtoException(interfaceType.asElement(), "" +
                    "No DtoImplementer available for codec \"" + dtoName
                    + "\"; cannot generate DTO class for " + interfaceType)))
            .collect(Collectors.toUnmodifiableSet());
    }

    private String packageNameOf(final CharSequence qualifiedName) {
        final var q1 = qualifiedName.length();
        var q0 = q1;
        int qx = 0;
        while (q0-- != 0) {
            var c = qualifiedName.charAt(q0);
            if (c == '.') {
                if (q0 + 1 == q1) {
                    throw new IllegalArgumentException("Not a qualified type name: \"" + qualifiedName + "\"");
                }
                c = qualifiedName.charAt(q0 + 1);
                if (Character.isUpperCase(c)) {
                    qx = q0;
                }
                else {
                    break;
                }
            }
        }
        return qualifiedName.subSequence(0, qx).toString();
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
        return dtoCodecs;
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

    public boolean isReadable() {
        return !readableCodecs.isEmpty();
    }

    public boolean isReadable(final DtoCodecSpec dtoCodec) {
        return readableCodecs.contains(dtoCodec);
    }

    public boolean isWritable() {
        return !writableCodecs.isEmpty();
    }

    public boolean isWritable(final DtoCodecSpec dtoCodec) {
        return writableCodecs.contains(dtoCodec);
    }

    @Override
    public String toString() {
        return simpleName;
    }
}
