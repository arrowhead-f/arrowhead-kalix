package se.arkalix.dto;

import com.squareup.javapoet.TypeName;
import se.arkalix.dto.types.DtoInterface;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DtoTarget {
    public static final String DATA_SUFFIX = "Dto";
    public static final String BUILDER_SUFFIX = "Builder";

    private final DtoInterface interfaceType;
    private final List<DtoProperty> properties;

    public DtoTarget(final DtoInterface interfaceType, final List<DtoProperty> properties) {
        this.interfaceType = Objects.requireNonNull(interfaceType, "interfaceType");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public DtoInterface interfaceType() {
        return interfaceType;
    }

    public List<DtoProperty> properties() {
        return properties;
    }

    public boolean isAnnotatedWith(final Class<? extends Annotation> annotationClass) {
        return interfaceType.isAnnotatedWith(annotationClass);
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
