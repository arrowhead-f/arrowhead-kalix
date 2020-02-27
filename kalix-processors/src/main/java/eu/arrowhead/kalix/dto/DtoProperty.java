package eu.arrowhead.kalix.dto;

import eu.arrowhead.kalix.dto.types.DtoType;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.Objects;

public class DtoProperty {
    private final ExecutableElement parentElement;
    private final String name;
    private final Map<Format, String> formatNames;
    private final DtoType type;
    private final boolean isOptional;

    private DtoProperty(final Builder builder) {
        parentElement = Objects.requireNonNull(builder.parentElement, "parentElement");
        name = Objects.requireNonNull(builder.name, "name");
        formatNames = Objects.requireNonNull(builder.formatNames, "formatNames");
        type = Objects.requireNonNull(builder.type, "type");
        isOptional = builder.isOptional;
    }

    public ExecutableElement parentElement() {
        return parentElement;
    }

    public String name() {
        return name;
    }

    public String nameFor(final Format format) {
        return formatNames.getOrDefault(format, name);
    }

    public DtoType type() {
        return type;
    }

    public TypeMirror asTypeMirror() {
        return type.asTypeMirror();
    }

    public boolean isOptional() {
        return isOptional;
    }

    public static class Builder {
        private ExecutableElement parentElement;
        private String name;
        private Map<Format, String> formatNames;
        private DtoType type;
        private boolean isOptional;

        public Builder parentElement(final ExecutableElement parentElement) {
            this.parentElement = parentElement;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder formatNames(final Map<Format, String> formatNames) {
            this.formatNames = formatNames;
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
