package eu.arrowhead.kalix.dto;

import eu.arrowhead.kalix.dto.types.DTOInterface;

import java.util.*;

public class DTOTarget {
    private final DTOInterface interfaceType;
    private final List<DTOProperty> properties;

    public DTOTarget(final DTOInterface interfaceType, final List<DTOProperty> properties) {
        this.interfaceType = interfaceType;
        this.properties = properties;
    }

    public DTOInterface interfaceType() {
        return interfaceType;
    }

    public List<DTOProperty> properties() {
        return properties;
    }

    public Set<Format> formats() {
        return interfaceType.formats();
    }

    public String simpleName() {
        return interfaceType.simpleNameDTO();
    }
}
