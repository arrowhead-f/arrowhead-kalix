package se.arkalix.dto.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import se.arkalix.dto.DtoTarget;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DtoInterface implements DtoType {
    private final Set<String> readableDtoEncodings;
    private final Set<String> writableDtoEncodings;
    private final Set<String> dtoEncodings;

    private final String simpleName;
    private final String dataSimpleName;
    private final String builderSimpleName;
    private final TypeName inputTypeName;
    private final TypeName outputTypeName;

    public DtoInterface(
        final DeclaredType interfaceType,
        final String[] readableDtoEncodings,
        final String[] writableDtoEncodings
    ) {
        this.readableDtoEncodings = Stream.of(readableDtoEncodings).collect(Collectors.toSet());
        this.writableDtoEncodings = Stream.of(writableDtoEncodings).collect(Collectors.toSet());

        dtoEncodings = new HashSet<>();
        dtoEncodings.addAll(this.readableDtoEncodings);
        dtoEncodings.addAll(this.writableDtoEncodings);

        final TypeElement interfaceElement = (TypeElement) interfaceType.asElement();
        simpleName = interfaceElement.getSimpleName().toString();
        dataSimpleName = simpleName + DtoTarget.DATA_SUFFIX;
        builderSimpleName = simpleName + DtoTarget.BUILDER_SUFFIX;
        inputTypeName = ClassName.get(packageNameOf(interfaceElement.getQualifiedName()), dataSimpleName);
        outputTypeName = TypeName.get(interfaceType);
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

    public String simpleName() {
        return simpleName;
    }

    public String dataSimpleName() {
        return dataSimpleName;
    }

    public String builderSimpleName() {
        return builderSimpleName;
    }

    public Set<String> encodings() {
        return dtoEncodings;
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

    public boolean isReadable(final String dtoEncoding) {
        return readableDtoEncodings.contains(dtoEncoding);
    }

    public boolean isWritable(final String dtoEncoding) {
        return writableDtoEncodings.contains(dtoEncoding);
    }

    @Override
    public String toString() {
        return simpleName;
    }
}
