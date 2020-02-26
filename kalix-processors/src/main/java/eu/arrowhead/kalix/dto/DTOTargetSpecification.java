package eu.arrowhead.kalix.dto;

import com.squareup.javapoet.TypeSpec;

import java.util.Objects;

public class DTOTargetSpecification {
    private final DTOTarget target;
    private final TypeSpec implementation;
    private final TypeSpec builder;

    public DTOTargetSpecification(final Builder builder) {
        this.target = builder.target;
        this.implementation = Objects.requireNonNull(builder.implementation);
        this.builder = Objects.requireNonNull(builder.builder);
    }

    public DTOTarget target() {
        return target;
    }

    public TypeSpec implementation() {
        return implementation;
    }

    public TypeSpec builder() {
        return builder;
    }

    public static class Builder {
        private final DTOTarget target;

        private TypeSpec implementation;
        private TypeSpec builder;

        public Builder(final DTOTarget target) {
            this.target = target;
        }

        public Builder implementation(TypeSpec implementation) {
            this.implementation = implementation;
            return this;
        }

        public Builder builder(TypeSpec builder) {
            this.builder = builder;
            return this;
        }

        public DTOTargetSpecification build() {
            return new DTOTargetSpecification(this);
        }
    }
}
