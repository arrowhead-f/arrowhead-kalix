package se.arkalix.dto;

import se.arkalix.dto.types.DtoDescriptor;
import se.arkalix.dto.types.DtoType;

import javax.lang.model.element.ExecutableElement;
import java.util.Map;
import java.util.Objects;

public class DtoProperty {
    private final ExecutableElement method;
    private final String name;
    private final Map<DtoCodec, String> dtoCodecToName;
    private final DtoType type;

    private DtoProperty(final Builder builder) {
        method = Objects.requireNonNull(builder.method, "method");
        name = Objects.requireNonNull(builder.name, "name");
        dtoCodecToName = Objects.requireNonNull(builder.dtoCodecToName, "dtoCodecToName");
        type = Objects.requireNonNull(builder.type, "type");
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

    public DtoDescriptor descriptor() {
        return type.descriptor();
    }

    public static class Builder {
        private ExecutableElement method;
        private String name;
        private Map<DtoCodec, String> dtoCodecToName;
        private DtoType type;

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

        public DtoProperty build() {
            return new DtoProperty(this);
        }
    }
}
