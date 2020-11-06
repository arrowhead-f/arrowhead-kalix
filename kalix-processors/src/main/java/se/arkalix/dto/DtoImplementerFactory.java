package se.arkalix.dto;

import com.squareup.javapoet.*;
import se.arkalix.dto.types.DtoCollection;
import se.arkalix.dto.types.DtoDescriptor;
import se.arkalix.dto.types.DtoSequence;
import se.arkalix.dto.util.Expander;
import se.arkalix.encoding.Encoding;
import se.arkalix.encoding.EncodingUnsupported;
import se.arkalix.encoding.MultiEncodable;
import se.arkalix.encoding.binary.BinaryReader;
import se.arkalix.encoding.binary.BinaryWriter;

import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public class DtoImplementerFactory {
    private final DtoImplementer[] implementers;

    public DtoImplementerFactory(final DtoImplementer... implementers) {
        this.implementers = implementers;
    }

    public DtoTargetSpecification createForTarget(final DtoTarget target) {
        final var interfaceType = target.interfaceType();

        final var implementationClassName = ClassName.bestGuess(target.dataSimpleName());
        final var implementation = TypeSpec.classBuilder(implementationClassName)
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
                    getter.addStatement(output.expand("($N) $N"), descriptor == DtoDescriptor.LIST ? "List" : "Map", name);

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

            if (descriptor == DtoDescriptor.LIST) {
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

        if (target.isComparable()) {
            final var equals = MethodSpec.methodBuilder("equals")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.OBJECT, "other", Modifier.FINAL)
                .returns(TypeName.BOOLEAN)
                .addCode("if (this == other) { return true; };\n")
                .addCode("if (other == null || getClass() != other.getClass()) { return false; };\n")
                .addCode("final $1T that = ($1T) other;\n", target.dataTypeName())
                .addCode("return ");

            var index = 0;
            for (final var property : target.properties()) {
                final var descriptor = property.descriptor();
                final var name = property.name();

                if (index++ != 0) {
                    equals.addCode(" &&\n    ");
                }
                if (descriptor.isPrimitiveUnboxed()) {
                    equals.addCode("$1N == that.$1N", name);
                }
                else if (descriptor == DtoDescriptor.ARRAY) {
                    equals.addCode("$1T.equals($2N, that.$2N)", Arrays.class, name);
                }
                else if (property.isOptional()) {
                    equals.addCode("$1T.equals($2N, that.$2N)", Objects.class, name);
                }
                else {
                    equals.addCode("$1N.equals(that.$1N)", name);
                }
            }

            implementation.addMethod(equals
                .addCode(";")
                .build());

            final var hashCode = MethodSpec.methodBuilder("hashCode")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT)
                .addCode("return $T.hash(", Objects.class);

            index = 0;
            for (final var property : target.properties()) {
                if (index++ != 0) {
                    hashCode.addCode(", ");
                }
                hashCode.addCode(property.name());
            }

            implementation.addMethod(hashCode
                .addCode(");")
                .build());
        }

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

        if (target.isReadable()) {
            final var decode = MethodSpec.methodBuilder("decode")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(implementationClassName)
                .addParameter(ClassName.get(BinaryReader.class), "reader", Modifier.FINAL)
                .addParameter(ClassName.get(Encoding.class), "encoding", Modifier.FINAL);

            for (final var encoding : target.interfaceType().readableEncodings()) {
                decode
                    .beginControlFlow("if (encoding == $T.$N)", Encoding.class, encoding.name())
                    .addStatement("return $N(reader)", encoding.decoderMethodName())
                    .endControlFlow();
            }

            implementation.addMethod(decode
                .addStatement("throw new $T(encoding)", EncodingUnsupported.class)
                .build());
        }

        if (target.isWritable()) {
            final var encode = MethodSpec.methodBuilder("encode")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(BinaryWriter.class), "writer", Modifier.FINAL)
                .addParameter(ClassName.get(Encoding.class), "encoding", Modifier.FINAL);

            for (final var encoding : target.interfaceType().writableEncodings()) {
                encode
                    .beginControlFlow("if (encoding == $T.$N)", Encoding.class, encoding.name())
                    .addStatement("$N(writer)", encoding.encoderMethodName())
                    .addStatement("return")
                    .endControlFlow();
            }

            implementation
                .addSuperinterface(ClassName.get(MultiEncodable.class))
                .addMethod(encode
                    .addStatement("throw new $T(encoding)", EncodingUnsupported.class)
                    .build());
        }

        // TODO: Make it possible to provide custom DtoImplementer classes, somehow.
        final var targetEncodings = target.encodings();
        for (final var implementer : implementers) {
            if (targetEncodings.contains(implementer.encoding())) {
                implementer.implementFor(target, implementation);
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
