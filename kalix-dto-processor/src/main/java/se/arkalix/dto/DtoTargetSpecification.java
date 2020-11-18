package se.arkalix.dto;

import com.squareup.javapoet.TypeSpec;

import java.util.Objects;

public class DtoTargetSpecification {
    private final TypeSpec implementation;
    private final TypeSpec builder;

    public DtoTargetSpecification(final Builder builder) {
        this.implementation = Objects.requireNonNull(builder.implementation);
        this.builder = Objects.requireNonNull(builder.builder);
    }

    public TypeSpec implementation() {
        return implementation;
    }

    public TypeSpec builder() {
        return builder;
    }

    public static class Builder {
        private TypeSpec implementation;
        private TypeSpec builder;

        public Builder implementation(final TypeSpec implementation) {
            this.implementation = implementation;
            return this;
        }

        public Builder builder(final TypeSpec builder) {
            this.builder = builder;
            return this;
        }

        public DtoTargetSpecification build() {
            return new DtoTargetSpecification(this);
        }
    }
}
