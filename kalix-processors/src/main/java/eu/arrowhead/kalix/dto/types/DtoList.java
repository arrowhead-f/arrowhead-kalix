package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;

public class DtoList implements DtoArrayOrList {
    private final DeclaredType type;
    private final DtoType element;

    public DtoList(final DeclaredType type, final DtoType element) {
        this.type = type;
        this.element = element;
    }

    @Override
    public DtoType element() {
        return element;
    }

    @Override
    public DtoDescriptor descriptor() {
        return DtoDescriptor.LIST;
    }

    @Override
    public DeclaredType asTypeMirror() {
        return type;
    }

    @Override
    public String toString() {
        return "List<" + element + ">";
    }
}