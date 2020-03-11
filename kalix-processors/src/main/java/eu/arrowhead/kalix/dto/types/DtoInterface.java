package eu.arrowhead.kalix.dto.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import eu.arrowhead.kalix.dto.DtoTarget;
import eu.arrowhead.kalix.dto.DataEncoding;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DtoInterface implements DtoType {
    private final Set<DataEncoding> readableDataEncodings;
    private final Set<DataEncoding> writableDataEncodings;
    private final Set<DataEncoding> dataEncodings;

    private final String simpleName;
    private final String dataSimpleName;
    private final String builderSimpleName;
    private final TypeName inputTypeName;
    private final TypeName outputTypeName;

    public DtoInterface(
        final DeclaredType interfaceType,
        final DataEncoding[] readableDataEncodings,
        final DataEncoding[] writableDataEncodings
    ) {
        this.readableDataEncodings = Stream.of(readableDataEncodings).collect(Collectors.toSet());
        this.writableDataEncodings = Stream.of(writableDataEncodings).collect(Collectors.toSet());

        dataEncodings = new HashSet<>();
        dataEncodings.addAll(this.readableDataEncodings);
        dataEncodings.addAll(this.writableDataEncodings);

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

    public Set<DataEncoding> encodings() {
        return dataEncodings;
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

    public boolean isReadable(final DataEncoding dataEncoding) {
        return readableDataEncodings.contains(dataEncoding);
    }

    public boolean isWritable(final DataEncoding dataEncoding) {
        return writableDataEncodings.contains(dataEncoding);
    }

    @Override
    public String toString() {
        return simpleName;
    }
}
