package eu.arrowhead.kalix.dto.types;

import eu.arrowhead.kalix.dto.Format;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DTOInterface implements DTOType {
    private final DeclaredType interfaceType;
    private final Set<Format> readableFormats;
    private final Set<Format> writableFormats;
    private final String simpleName;
    private final String simpleNameDTO;
    private final Set<Format> formats;

    public DTOInterface(
        final DeclaredType interfaceType,
        final Format[] readableFormats,
        final Format[] writableFormats
    ) {
        this.interfaceType = interfaceType;
        this.readableFormats = Stream.of(readableFormats).collect(Collectors.toSet());
        this.writableFormats = Stream.of(writableFormats).collect(Collectors.toSet());

        final TypeElement interfaceElement = (TypeElement) interfaceType.asElement();
        simpleName = interfaceElement.getSimpleName().toString();
        simpleNameDTO = simpleName + "DTO";
        formats = new HashSet<>();
        formats.addAll(this.readableFormats);
        formats.addAll(this.writableFormats);
    }

    public String simpleName() {
        return simpleName;
    }

    public String simpleNameDTO() {
        return simpleNameDTO;
    }

    public Set<Format> formats() {
        return formats;
    }

    @Override
    public DeclaredType type() {
        return interfaceType;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public boolean isReadable() {
        return readableFormats.size() > 0;
    }

    @Override
    public boolean isReadable(final Format format) {
        return readableFormats.contains(format);
    }

    @Override
    public boolean isWritable() {
        return writableFormats.size() > 0;
    }

    @Override
    public boolean isWritable(final Format format) {
        return writableFormats.contains(format);
    }
}
