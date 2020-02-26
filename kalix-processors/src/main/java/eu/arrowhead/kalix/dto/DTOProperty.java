package eu.arrowhead.kalix.dto;

import eu.arrowhead.kalix.dto.types.DTOType;

import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.Objects;

public class DTOProperty {
    private final String name;
    private final Map<Format, String> formatNames;
    private final DTOType type;
    private final boolean isOptional;

    private DTOProperty(final Builder builder) {
        name = Objects.requireNonNull(builder.name, "name");
        formatNames = Objects.requireNonNull(builder.formatNames, "formatNames");
        type = Objects.requireNonNull(builder.type, "type");
        isOptional = builder.isOptional;
    }

    public String name() {
        return name;
    }

    public String nameFor(final Format format) {
        return formatNames.getOrDefault(format, name);
    }

    public DTOType type() {
        return type;
    }

    public TypeMirror typeMirror() {
        return type.type();
    }

    public boolean isOptional() {
        return isOptional;
    }

    public static class Builder {
        private String name;
        private Map<Format, String> formatNames;
        private DTOType type;
        private boolean isOptional;

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder formatNames(final Map<Format, String> formatNames) {
            this.formatNames = formatNames;
            return this;
        }

        public Builder type(final DTOType type) {
            this.type = type;
            return this;
        }

        public Builder isOptional(final boolean isOptional) {
            this.isOptional = isOptional;
            return this;
        }

        public DTOProperty build() {
            return new DTOProperty(this);
        }
    }

    /*
    public void addConstructorBuilderStatement(final MethodSpec.Builder constructor) {
        if (isOptional() || (this instanceof PropertyPrimitive) && !((PropertyPrimitive) this).isBoxed()) {
            constructor.addStatement("this.$N = builder.$N", name());
        }
        else {
            constructor.addStatement("this." + name +
                    " = $T.requireNonNull(builder." + name +
                    ", \"Expected " + name + "\")",
                Objects.class);
        }
    }

    public MethodSpec specifyBuilderSetter() {
        return MethodSpec.methodBuilder(name)
            .addModifiers(Modifier.PUBLIC)
            .returns(ClassName.bestGuess("Builder"))
            .addParameter(ParameterSpec.builder(TypeName.get(type), name, Modifier.FINAL).build())
            .addStatement("this.$N = $N", name)
            .addStatement("return this")
            .build();
    }

    public MethodSpec specifyGetter() {
        return MethodSpec.methodBuilder(name)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.get(type))
            .addStatement("return $N", name)
            .build();
    }
    */
}
