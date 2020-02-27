package eu.arrowhead.kalix.dto.types;

import eu.arrowhead.kalix.dto.DtoTarget;
import eu.arrowhead.kalix.dto.Format;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DtoInterface implements DtoType {
    private final DeclaredType interfaceType;
    private final Set<Format> readableFormats;
    private final Set<Format> writableFormats;
    private final String simpleName;
    private final String targetSimpleName;
    private final Set<Format> formats;

    public DtoInterface(
        final DeclaredType interfaceType,
        final Format[] readableFormats,
        final Format[] writableFormats
    ) {
        this.interfaceType = interfaceType;
        this.readableFormats = Stream.of(readableFormats).collect(Collectors.toSet());
        this.writableFormats = Stream.of(writableFormats).collect(Collectors.toSet());

        final TypeElement interfaceElement = (TypeElement) interfaceType.asElement();
        simpleName = interfaceElement.getSimpleName().toString();
        targetSimpleName = simpleName + DtoTarget.NAME_SUFFIX;
        formats = new HashSet<>();
        formats.addAll(this.readableFormats);
        formats.addAll(this.writableFormats);
    }

    public String simpleName() {
        return simpleName;
    }

    public String targetSimpleName() {
        return targetSimpleName;
    }

    public Set<Format> formats() {
        return formats;
    }

    @Override
    public DtoDescriptor descriptor() {
        return DtoDescriptor.INTERFACE;
    }

    @Override
    public DeclaredType asTypeMirror() {
        return interfaceType;
    }

    public boolean isReadable(final Format format) {
        return readableFormats.contains(format);
    }

    public boolean isWritable(final Format format) {
        return writableFormats.contains(format);
    }

    @Override
    public String toString() {
        return simpleName;
    }
}
