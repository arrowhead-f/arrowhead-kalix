package eu.arrowhead.kalix.dto;

import com.squareup.javapoet.*;
import eu.arrowhead.kalix.dto.types.DtoDescriptor;
import eu.arrowhead.kalix.dto.types.DtoList;
import eu.arrowhead.kalix.dto.util.Expander;

import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

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
                    : outputTypeName);

            final Expander modifier = property.isOptional()
                ? (expression) -> "return Optional.ofNullable(" + expression + ")"
                : (expression) -> "return " + expression;

            switch (property.descriptor()) {
            case ARRAY:
                getter.addStatement(modifier.expand("$N.clone()"), name);
                break;

            case LIST:
                getter.addStatement(modifier.expand("(List) $N"), name);
                break;

            case MAP:
                getter.addStatement(modifier.expand("(Map) $N"), name);
                break;

            default:
                getter.addStatement(modifier.expand("$N"), name);
                break;
            }

            implementation.addMethod(getter.build());

            builder.addField(FieldSpec.builder(inputTypeName, name).build());
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

            if (descriptor == DtoDescriptor.LIST) {
                final var element = ((DtoList) property.type()).element();
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

            if (property.isOptional() || property.type().descriptor().isPrimitive()) {
                constructor.addStatement("this.$1N = builder.$1N", name);
            }
            else {
                constructor.addStatement("this.$1N = $2T.requireNonNull(builder.$1N, \"$1N\")",
                    name, Objects.class);
            }
        });


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
