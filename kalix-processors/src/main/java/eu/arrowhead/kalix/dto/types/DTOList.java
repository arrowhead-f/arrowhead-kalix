package eu.arrowhead.kalix.dto.types;

import javax.lang.model.type.DeclaredType;

public class DTOList implements DTOArrayOrList {
    private final DeclaredType type;
    private final DTOType element;

    public DTOList(final DeclaredType type, final DTOType element) {
        this.type = type;
        this.element = element;
    }

    @Override
    public DTOType element() {
        return element;
    }

    @Override
    public String name() {
        return "List<" + element.name() + ">";
    }

    @Override
    public DeclaredType asTypeMirror() {
        return type;
    }
}