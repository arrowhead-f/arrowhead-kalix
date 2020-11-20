package se.arkalix.dto;

import com.squareup.javapoet.*;
import se.arkalix.codec.CodecType;
import se.arkalix.codec.CodecUnsupported;
import se.arkalix.codec.MultiEncodable;
import se.arkalix.codec.binary.BinaryReader;
import se.arkalix.codec.binary.BinaryWriter;
import se.arkalix.dto.types.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DtoGenerator {
    private final Map<DtoCodec, DtoGeneratorBackend> backends;

    public DtoGenerator(final DtoGeneratorBackend... backends) {
        this.backends = Arrays.stream(backends)
            .collect(Collectors.toUnmodifiableMap(DtoGeneratorBackend::codec, backend -> backend));
    }

    public void writeTo(final DtoTarget target, final String packageName, final Filer filer) throws IOException {
        final var interface_ = target.interface_();
        final var interfaceElement = interface_.element();
        final var interfaceTypeName = interfaceElement.asType();

        final var implementationClassName = target.typeName();
        final var implementation = TypeSpec.classBuilder(implementationClassName)
            .addJavadoc("{@link $T} Data Transfer Object (DTO).", interfaceTypeName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(interfaceTypeName);

        final var builderClassName = ClassName.bestGuess("Builder");
        final var builder = TypeSpec.classBuilder(builderClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        final var constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(ParameterSpec.builder(builderClassName, "builder")
                .addModifiers(Modifier.FINAL)
                .build());

        target.properties().forEach(property -> {
            final var descriptor = property.descriptor();
            final var name = property.name();
            final var type = property.type();
            final var pInterfaceTypeName = type.originalTypeName();
            final var pGeneratedTypeName = type.generatedTypeName();

            // DTO property field.
            implementation.addField(FieldSpec.builder(
                new DtoDescriptorRouter<TypeName>() {
                    @Override
                    public TypeName onAny(final DtoDescriptor descriptor) {
                        return pGeneratedTypeName;
                    }

                    @Override
                    public TypeName onOptional(final DtoDescriptor descriptor) {
                        return ((DtoTypeOptional) type).valueType().generatedTypeName();
                    }
                }.route(descriptor), name)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build());

            // DTO getter method(s).
            implementation.addMethod(new DtoDescriptorRouter<MethodSpec>() {
                private final MethodSpec.Builder getter = MethodSpec.methodBuilder(name)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(pInterfaceTypeName);

                @Override
                public MethodSpec onAny(final DtoDescriptor descriptor) {
                    return getter.addStatement("return $N", name)
                        .build();
                }

                @Override
                public MethodSpec onArray(final DtoDescriptor descriptor) {
                    return getter.addStatement("return $N.clone()", name)
                        .build();
                }

                @Override
                public MethodSpec onCollection(final DtoDescriptor descriptor) {
                    final var collection = ((DtoTypeCollection) type);
                    if (collection.containsInterfaceType()) {
                        implementation.addMethod(MethodSpec.methodBuilder(name + "AsDtos")
                            .addModifiers(Modifier.PUBLIC)
                            .addJavadoc("@see #$N()", name)
                            .returns(type.generatedTypeName())
                            .addStatement("return $N", name)
                            .build());

                        return getter
                            .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                                .addMember("value", "\"unchecked\"")
                                .build())
                            .addStatement("return ($N) $N", descriptor == DtoDescriptor.LIST ? "List" : "Map", name)
                            .build();
                    }
                    else {
                        return onAny(descriptor);
                    }
                }

                @Override
                public MethodSpec onOptional(final DtoDescriptor descriptor) {
                    final var valueType = ((DtoTypeOptional) type).valueType();
                    if (valueType.descriptor() == DtoDescriptor.INTERFACE) {
                        implementation.addMethod(MethodSpec.methodBuilder(name + "AsDto")
                            .addModifiers(Modifier.PUBLIC)
                            .addJavadoc("@see #$N()", name)
                            .returns(pGeneratedTypeName)
                            .addStatement("return Optional.ofNullable($N)", name)
                            .build());
                    }
                    return getter.addStatement("return Optional.ofNullable($N)", name)
                        .build();
                }
            }.route(descriptor));

            // Builder property field.
            builder.addField(FieldSpec.builder(
                new DtoDescriptorRouter<TypeName>() {
                    @Override
                    public TypeName onAny(final DtoDescriptor descriptor) {
                        return descriptor.isPrimitiveUnboxed()
                            ? pGeneratedTypeName.box()
                            : pGeneratedTypeName;
                    }

                    @Override
                    public TypeName onOptional(final DtoDescriptor descriptor) {
                        return ((DtoTypeOptional) type).valueType().generatedTypeName();
                    }
                }.route(descriptor), name)
                .addModifiers(Modifier.PRIVATE)
                .build());

            // Builder setter method(s)
            builder.addMethod(new DtoDescriptorRouter<MethodSpec>() {
                final MethodSpec.Builder setter = MethodSpec.methodBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(builderClassName)
                    .addStatement("this.$1N = $1N", name)
                    .addStatement("return this");

                @Override
                public MethodSpec onAny(final DtoDescriptor descriptor) {
                    return setter
                        .addParameter(pGeneratedTypeName, name, Modifier.FINAL)
                        .build();
                }

                @Override
                public MethodSpec onArray(final DtoDescriptor descriptor) {
                    setter.varargs();
                    return onAny(descriptor);
                }

                @Override
                public MethodSpec onList(final DtoDescriptor descriptor) {
                    final var itemType = ((DtoTypeSequence) type).itemType();
                    if (!itemType.descriptor().isCollection()) {
                        final var itemTypeName = itemType.generatedTypeName();
                        builder.addMethod(MethodSpec.methodBuilder(name)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(ArrayTypeName.of(itemTypeName), name, Modifier.FINAL)
                            .varargs()
                            .returns(builderClassName)
                            .addStatement("this.$1N = $2T.asList($1N)", name, Arrays.class)
                            .addStatement("return this")
                            .build());
                    }
                    return onAny(descriptor);
                }

                @Override
                public MethodSpec onOptional(final DtoDescriptor descriptor) {
                    final var valueType = ((DtoTypeOptional) type).valueType();
                    return setter
                        .addParameter(valueType.generatedTypeName(), name, Modifier.FINAL)
                        .build();
                }
            }.route(descriptor));

            // DTO constructor statement.
            switch (property.descriptor()) {
            case ARRAY:
                constructor.addStatement("this.$1N = builder.$1N == null ? new $2T{} : builder.$1N",
                    name, pInterfaceTypeName);
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

            case OPTIONAL:
                constructor.addStatement("this.$1N = builder.$1N", name);
                break;

            default:
                constructor.addStatement("this.$1N = $2T.requireNonNull(builder.$1N, \"$1N\")",
                    name, Objects.class);
                break;
            }
        });

        if (interfaceElement.getAnnotation(DtoEqualsHashCode.class) != null) {
            final var equals = MethodSpec.methodBuilder("equals")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.OBJECT, "other", Modifier.FINAL)
                .returns(TypeName.BOOLEAN)
                .addCode("if (this == other) { return true; };\n")
                .addCode("if (other == null || getClass() != other.getClass()) { return false; };\n")
                .addCode("final $1T that = ($1T) other;\n", implementationClassName)
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
                else if (descriptor == DtoDescriptor.OPTIONAL) {
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

        if (interfaceElement.getAnnotation(DtoToString.class) != null) {
            final var toString = MethodSpec.methodBuilder("toString")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(String.class))
                .addCode("return \"$T{\" +\n", interfaceTypeName);

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
                case ARRAY:
                    toString.addCode("\" + $T.toString($N) +\n", Arrays.class, name);
                    break;

                case STRING:
                    toString.addCode("'\" + $N + '\\'' +\n", name);
                    break;

                case OPTIONAL:
                    final var valueType = ((DtoTypeOptional) property.type()).valueType();
                    if (valueType.descriptor() == DtoDescriptor.ARRAY) {
                        toString.addCode("\" + ($2N == null ? \"null\" : $1T.toString($2N)) +\n", Arrays.class, name);
                        break;

                    }
                default:
                    toString.addCode("\" + $N +\n", name);
                    break;
                }
            }

            implementation.addMethod(toString
                .addCode("    '}';\n")
                .build());
        }

        final var dtoReadableAs = interfaceElement.getAnnotation(DtoReadableAs.class);
        if (dtoReadableAs != null) {
            final var decode = MethodSpec.methodBuilder("decode")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(implementationClassName)
                .addParameter(ClassName.get(BinaryReader.class), "reader", Modifier.FINAL)
                .addParameter(ClassName.get(CodecType.class), "codec", Modifier.FINAL);

            for (final var codec : dtoReadableAs.value()) {
                final var backend = getBackendByCodecOrThrow(codec);
                backend.generateDecodeMethodFor(target, implementation);
                decode
                    .beginControlFlow("if (codec == $T.$N)", CodecType.class, codec.name())
                    .addStatement("return $N(reader)", backend.decodeMethodName())
                    .endControlFlow();
            }

            implementation.addMethod(decode
                .addStatement("throw new $T(codec)", CodecUnsupported.class)
                .build());
        }

        final var dtoWritableAs = interfaceElement.getAnnotation(DtoWritableAs.class);
        if (dtoWritableAs != null) {
            final var encode = MethodSpec.methodBuilder("encode")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(BinaryWriter.class), "writer", Modifier.FINAL)
                .addParameter(ClassName.get(CodecType.class), "codec", Modifier.FINAL);

            for (final var codec : dtoWritableAs.value()) {
                final var backend = getBackendByCodecOrThrow(codec);
                backend.generateEncodeMethodFor(target, implementation);
                encode
                    .beginControlFlow("if (codec == $T.$N)", CodecType.class, codec.name())
                    .addStatement("$N(writer)", backend.encodeMethodName())
                    .addStatement("return")
                    .endControlFlow();
            }

            implementation
                .addSuperinterface(ClassName.get(MultiEncodable.class))
                .addMethod(encode
                    .addStatement("throw new $T(codec)", CodecUnsupported.class)
                    .build());
        }

        JavaFile.builder(packageName, implementation
            .addMethod(constructor.build())
            .addType(builder
                .addMethod(MethodSpec.methodBuilder("build")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(implementationClassName)
                    .addStatement("return new $T(this)", implementationClassName)
                    .build())
                .build())
            .build())
            .indent("    ")
            .build()
            .writeTo(filer);
    }

    private DtoGeneratorBackend getBackendByCodecOrThrow(final DtoCodec codec) {
        final var backend = backends.get(codec);
        if (backend == null) {
            throw new IllegalStateException("No code generation backend " +
                "available for codec \"" + codec + "\"; cannot generate DTO " +
                "class");
        }
        return backend;
    }
}
