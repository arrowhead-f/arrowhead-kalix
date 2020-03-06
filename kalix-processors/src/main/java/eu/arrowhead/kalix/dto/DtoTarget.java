package eu.arrowhead.kalix.dto;

import eu.arrowhead.kalix.dto.types.DtoInterface;

import java.util.*;

public class DtoTarget {
    public static final String NAME_SUFFIX = "Data";

    private final DtoInterface interfaceType;
    private final List<DtoProperty> properties;

    public DtoTarget(final DtoInterface interfaceType, final List<DtoProperty> properties) {
        this.interfaceType = interfaceType;
        this.properties = properties;
    }

    public DtoInterface interfaceType() {
        return interfaceType;
    }

    public List<DtoProperty> properties() {
        return properties;
    }

    public Set<DataEncoding> encodings() {
        return interfaceType.encodings();
    }

    public String simpleName() {
        return interfaceType.targetSimpleName();
    }
}
