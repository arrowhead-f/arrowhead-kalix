package se.arkalix.dto;

import com.squareup.javapoet.TypeName;
import se.arkalix.dto.types.DtoInterface;

import java.util.List;
import java.util.Set;

public class DtoTarget {
    public static final String DATA_SUFFIX = "Dto";
    public static final String BUILDER_SUFFIX = "Builder";

    private final DtoInterface interfaceType;
    private final List<DtoProperty> properties;
    private final boolean isPrintable;

    public DtoTarget(final DtoInterface interfaceType, final List<DtoProperty> properties, final boolean isPrintable) {
        this.interfaceType = interfaceType;
        this.properties = properties;
        this.isPrintable = isPrintable;
    }

    public DtoInterface interfaceType() {
        return interfaceType;
    }

    public List<DtoProperty> properties() {
        return properties;
    }

    public boolean isPrintable() {
        return isPrintable;
    }

    public Set<DtoEncoding> encodings() {
        return interfaceType.encodings();
    }

    public String dataSimpleName() {
        return interfaceType.dataSimpleName();
    }

    public TypeName dataTypeName() {
        return interfaceType.inputTypeName();
    }
}
