package se.arkalix.dto;

import com.squareup.javapoet.TypeName;
import se.arkalix.dto.types.DtoInterface;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DtoTarget {
    public static final String DATA_SUFFIX = "Dto";
    public static final String BUILDER_SUFFIX = "Builder";

    private final DtoInterface interfaceType;
    private final List<DtoProperty> properties;
    private final boolean isComparable;
    private final boolean isPrintable;

    public DtoTarget(
        final DtoInterface interfaceType,
        final List<DtoProperty> properties,
        final boolean isComparable,
        final boolean isPrintable)
    {
        this.interfaceType = Objects.requireNonNull(interfaceType, "interfaceType");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.isComparable = isComparable;
        this.isPrintable = isPrintable;
    }

    public DtoInterface interfaceType() {
        return interfaceType;
    }

    public List<DtoProperty> properties() {
        return properties;
    }

    public boolean isComparable() {
        return isComparable;
    }

    public boolean isPrintable() {
        return isPrintable;
    }

    public boolean isReadable() {
        return interfaceType.isReadable();
    }

    public boolean isWritable() {
        return interfaceType.isWritable();
    }

    public Set<DtoCodecSpec> codecs() {
        return interfaceType.codecs();
    }

    public String dataSimpleName() {
        return interfaceType.dataSimpleName();
    }

    public TypeName dataTypeName() {
        return interfaceType.inputTypeName();
    }

    @Override
    public String toString() {
        return interfaceType.inputTypeName().toString();
    }
}
