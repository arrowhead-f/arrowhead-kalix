package se.arkalix.dto;

import com.squareup.javapoet.*;
import se.arkalix.dto.types.DtoCollection;
import se.arkalix.dto.types.DtoDescriptor;
import se.arkalix.dto.types.DtoSequence;
import se.arkalix.dto.util.Expander;

import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static se.arkalix.dto.types.DtoDescriptor.LIST;

public class DtoSpecificationFactory {
    private final DtoSpecificationEncoding[] specificationEncodings;

    public DtoSpecificationFactory(final DtoSpecificationEncoding... specificationEncodings) {
        this.specificationEncodings = specificationEncodings;
    }

    public DtoTargetSpecification createForTarget(final DtoTarget target) throws DtoException {
        final var interfaceType = target.interfaceType();

        final var implementation = TypeSpec.classBuilder(target.dataSimpleName())
            .addJavadoc("{@link $N} Data Transfer Object (DTO).", interfaceType.simpleName())
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(interfaceType.outputTypeName());

        final var builderSimpleName = interfaceType.builderSimpleName();
        final var builderClassName = ClassName.bestGuess(builderSimpleName);
        final var builder = TypeSpec.classBuilder(builderSimpleName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        final var constructor = MethodSpec.constructorBuilder()
            .addParameter(ParameterSpec.builder(builderClassName, "builder")
                .addModifiers(Modifier.FINAL)
                .build());

        target.properties().forEach(property -> {
            final var descriptor = property.descriptor();
            final var name = property.name();
            final var inputTypeName = property.inputTypeName();
            final var outputTypeName = property.outputTypeName();

            implementation.addField(inputTypeName, name, Modifier.PRIVATE, Modifier.FINAL);
            final var getter = MethodSpec.methodBuilder(name)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(property.isOptional()
                    ? ParameterizedTypeName.get(ClassName.get(Optional.class), outputTypeName)
                    : descriptor.isCollection() ? outputTypeName : inputTypeName);

            final Expander output;

            if (property.isOptional()) {
                output = (expression) -> "return Optional.ofNullable(" + expression + ')';

                if (descriptor == DtoDescriptor.INTERFACE) {
                    implementation.addMethod(MethodSpec.methodBuilder(name + "AsDto")
                        .addModifiers(Modifier.PUBLIC)
                        .addJavadoc("@see #$N()", name)
                        .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), inputTypeName))
                        .addStatement(output.expand("$N"), name)
                        .build());
                }
            }
            else {
                output = (expression) -> "return " + expression;
            }

            switch (descriptor) {
            case ARRAY:
                getter.addStatement(output.expand("$N.clone()"), name);
                break;

            case LIST:
            case MAP:
                if (((DtoCollection) property.type()).containsInterfaceType()) {
                    getter.addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "\"unchecked\"").build());
                    getter.addStatement(output.expand("($N) $N"), descriptor == LIST ? "List" : "Map", name);

                    implementation.addMethod(MethodSpec.methodBuilder(name + "AsDtos")
                        .addModifiers(Modifier.PUBLIC)
                        .addJavadoc("@see #$N()", name)
                        .returns(inputTypeName)
                        .addStatement("return $N", name)
                        .build());
                }
                else {
                    getter.addStatement(output.expand("$N"), name);

                }
                break;

            default:
                getter.addStatement(output.expand("$N"), name);
                break;
            }

            implementation.addMethod(getter.build());

            final var builderFieldTypeName = property.descriptor().isPrimitive()
                ? property.inputTypeName().box()
                : property.inputTypeName();

            builder.addField(FieldSpec.builder(builderFieldTypeName, name).build());
            final var fieldSetter = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(inputTypeName, name, Modifier.FINAL)
                .returns(builderClassName)
                .addStatement("this.$1N = $1N", name)
                .addStatement("return this");

            if (descriptor == DtoDescriptor.ARRAY) {
                fieldSetter.varargs();
            }

            builder.addMethod(fieldSetter.build());

            if (descriptor == LIST) {
                final var element = ((DtoSequence) property.type()).element();
                if (!element.descriptor().isCollection()) {
                    final var elementTypeName = element.inputTypeName();

                    builder.addMethod(MethodSpec.methodBuilder(name)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ArrayTypeName.of(elementTypeName), name, Modifier.FINAL)
                        .varargs()
                        .returns(builderClassName)
                        .addStatement("this.$1N = $2T.asList($1N)", name, Arrays.class)
                        .addStatement("return this")
                        .build());
                }
            }

            if (property.isOptional()) {
                constructor.addStatement("this.$1N = builder.$1N", name);
            }
            else {
                switch (property.descriptor()) {
                case ARRAY:
                    constructor.addStatement("this.$1N = builder.$1N == null " +
                            "? new $2T{} : builder.$1N",
                        name, inputTypeName);
                    break;

                case LIST:
                    constructor.addStatement("this.$1N = builder.$1N == null || builder.$1N.size() == 0 " +
                            "? $2T.emptyList() : $2T.unmodifiableList(builder.$1N)",
                        name, Collections.class);
                    break;

                case MAP:
                    constructor.addStatement("this.$1N = builder.$1N == null || builder.$1N.size() == 0 " +
                            "? $2T.emptyMap() : $2T.unmodifiableMap(builder.$1N)",
                        name, Collections.class);
                    break;

                default:
                    constructor.addStatement("this.$1N = $2T.requireNonNull(builder.$1N, \"$1N\")",
                        name, Objects.class);
                    break;
                }
            }
        });

        if (target.isPrintable()) {
            final var toString = MethodSpec.methodBuilder("toString")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(String.class))
                .addCode("return \"$N{\" +\n", interfaceType.simpleName());

            var index = 0;
            for (final var property : target.properties()) {
                final var name = property.name();

                if (index++ != 0) {
                    toString.addCode("    \", $N=", name);
                }
                else {
                    toString.addCode("    \"$N=", name);
                }

                switch (property.descriptor()) {
                case STRING:
                    toString.addCode("'\" + $N + '\\'' +\n", name);
                    break;

                case ARRAY:
                    if (property.isOptional()) {
                        toString.addCode("\" + ($2N == null ? \"null\" : $1T.toString($2N)) +\n", Arrays.class, name);
                    }
                    else {
                        toString.addCode("\" + $T.toString($N) +\n", Arrays.class, name);
                    }
                    break;

                default:
                    toString.addCode("\" + $N +\n", name);
                    break;
                }
            }

            implementation.addMethod(toString
                .addCode("    '}';\n")
                .build());
        }

        final var targetEncodings = target.encodings();
        for (final var specificationEncodings : specificationEncodings) {
            if (targetEncodings.contains(specificationEncodings.encoding())) {
                specificationEncodings.implementFor(target, implementation);
            }
        }

        return new DtoTargetSpecification.Builder(target)
            .implementation(implementation
                .addMethod(constructor.build())
                .build())
            .builder(builder
                .addMethod(MethodSpec.methodBuilder("build")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(target.dataTypeName())
                    .addStatement("return new $N(this)", target.dataSimpleName())
                    .build())
                .build())
            .build();
    }
}
