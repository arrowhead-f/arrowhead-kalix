package se.arkalix.dto;

import se.arkalix.dto.types.DtoInterface;

import java.util.List;
import java.util.Objects;

public class DtoTarget {
    public static final String DATA_SUFFIX = "Dto";
    public static final String BUILDER_SUFFIX = "Builder";

    private final DtoInterface dtoInterface;
    private final List<DtoProperty> properties;

    public DtoTarget(final DtoInterface dtoInterface, final List<DtoProperty> properties) {
        this.dtoInterface = Objects.requireNonNull(dtoInterface, "dtoInterface");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public DtoInterface dtoInterface() {
        return dtoInterface;
    }

    public List<DtoProperty> dtoProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return dtoInterface.inputTypeName().toString();
    }
}
