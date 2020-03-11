package eu.arrowhead.kalix.dto;

import com.squareup.javapoet.TypeName;
import eu.arrowhead.kalix.dto.types.DtoInterface;

import java.util.*;

public class DtoTarget {
    public static final String DATA_SUFFIX = "Data";
    public static final String BUILDER_SUFFIX = "Builder";

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

    public String builderSimpleName() {
        return interfaceType.builderSimpleName();
    }

    public String dataSimpleName() {
        return interfaceType.dataSimpleName();
    }
    public TypeName dataTypeName() {
        return interfaceType.inputTypeName();
    }
}
