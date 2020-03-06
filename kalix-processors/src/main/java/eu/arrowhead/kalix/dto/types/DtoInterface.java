package eu.arrowhead.kalix.dto.types;

import eu.arrowhead.kalix.dto.DtoTarget;
import eu.arrowhead.kalix.dto.DataEncoding;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DtoInterface implements DtoType {
    private final DeclaredType interfaceType;
    private final Set<DataEncoding> readableDataEncodings;
    private final Set<DataEncoding> writableDataEncodings;
    private final String simpleName;
    private final String targetSimpleName;
    private final Set<DataEncoding> dataEncodings;

    public DtoInterface(
        final DeclaredType interfaceType,
        final DataEncoding[] readableDataEncodings,
        final DataEncoding[] writableDataEncodings
    ) {
        this.interfaceType = interfaceType;
        this.readableDataEncodings = Stream.of(readableDataEncodings).collect(Collectors.toSet());
        this.writableDataEncodings = Stream.of(writableDataEncodings).collect(Collectors.toSet());

        final TypeElement interfaceElement = (TypeElement) interfaceType.asElement();
        simpleName = interfaceElement.getSimpleName().toString();
        targetSimpleName = simpleName + DtoTarget.NAME_SUFFIX;
        dataEncodings = new HashSet<>();
        dataEncodings.addAll(this.readableDataEncodings);
        dataEncodings.addAll(this.writableDataEncodings);
    }

    public String simpleName() {
        return simpleName;
    }

    public String targetSimpleName() {
        return targetSimpleName;
    }

    public Set<DataEncoding> encodings() {
        return dataEncodings;
    }

    @Override
    public DtoDescriptor descriptor() {
        return DtoDescriptor.INTERFACE;
    }

    @Override
    public DeclaredType asTypeMirror() {
        return interfaceType;
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
