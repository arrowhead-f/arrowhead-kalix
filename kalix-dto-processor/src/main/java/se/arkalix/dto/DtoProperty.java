package se.arkalix.dto;

import com.squareup.javapoet.TypeName;
import se.arkalix.dto.types.DtoDescriptor;
import se.arkalix.dto.types.DtoType;

import javax.lang.model.element.ExecutableElement;
import java.util.Map;
import java.util.Objects;

public class DtoProperty implements DtoType {
    private final ExecutableElement method;
    private final String name;
    private final Map<DtoCodec, String> dtoCodecToName;
    private final DtoType type;
    private final boolean isOptional;

    private DtoProperty(final Builder builder) {
        method = Objects.requireNonNull(builder.method, "method");
        name = Objects.requireNonNull(builder.name, "name");
        dtoCodecToName = Objects.requireNonNull(builder.dtoCodecToName, "dtoCodecToName");
        type = Objects.requireNonNull(builder.type, "type");
        isOptional = builder.isOptional;
    }

    public ExecutableElement method() {
        return method;
    }

    public String name() {
        return name;
    }

    public String nameFor(final DtoCodec codecType) {
        return dtoCodecToName.getOrDefault(codecType, name);
    }

    public DtoType type() {
        return type;
    }

    @Override
    public DtoDescriptor descriptor() {
        return type.descriptor();
    }

    @Override
    public TypeName inputTypeName() {
        return type.inputTypeName();
    }

    @Override
    public TypeName outputTypeName() {
        return type.outputTypeName();
    }

    public boolean isOptional() {
        return isOptional;
    }

    public static class Builder {
        private ExecutableElement method;
        private String name;
        private Map<DtoCodec, String> dtoCodecToName;
        private DtoType type;
        private boolean isOptional;

        public Builder method(final ExecutableElement method) {
            this.method = method;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder dtoCodecToName(final Map<DtoCodec, String> dtoCodecToName) {
            this.dtoCodecToName = dtoCodecToName;
            return this;
        }

        public Builder type(final DtoType type) {
            this.type = type;
            return this;
        }

        public Builder isOptional(final boolean isOptional) {
            this.isOptional = isOptional;
            return this;
        }

        public DtoProperty build() {
            return new DtoProperty(this);
        }
    }
}
