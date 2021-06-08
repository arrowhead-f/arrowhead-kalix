package se.arkalix.dto;

import com.squareup.javapoet.ClassName;
import se.arkalix.dto.types.DtoTypeInterface;

import java.util.List;
import java.util.Objects;

public class DtoTarget {
    public static final String DATA_SUFFIX = "Dto";

    private final DtoTypeInterface interface_;
    private final List<DtoProperty> properties;

    public DtoTarget(final DtoTypeInterface interface_, final List<DtoProperty> properties) {
        this.interface_ = Objects.requireNonNull(interface_, "interface_");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public DtoTypeInterface interface_() {
        return interface_;
    }

    public List<DtoProperty> properties() {
        return properties;
    }

    @Override
    public String toString() {
        return interface_.originalTypeName().toString();
    }

    public ClassName typeName() {
        return interface_.generatedTypeName();
    }
}
