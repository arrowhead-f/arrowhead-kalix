package eu.arrowhead.kalix.processors.io;

import com.squareup.javapoet.*;
import eu.arrowhead.kalix.dto.ReadableDTO;
import eu.arrowhead.kalix.dto.WritableDTO;
import eu.arrowhead.kalix.dto.Format;

import javax.lang.model.element.Modifier;
import java.util.Objects;
import java.util.Optional;

public class DTOSpecifier {
    private DTOSpecifier() {}

    public static TypeSpec specifyClass(final DTOClass dtoClass) {
        final var origin = dtoClass.origin();
        final var root = TypeSpec.classBuilder(dtoClass.name())
            .addJavadoc("{@link " + origin.getSimpleName() + "} Data Transfer " +
                "Object (DTO).")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(TypeName.get(origin.asType()));

        if (dtoClass.isToBeDecodableAs(Format.JSON)) {
            root
                .addSuperinterface(ReadableDTO.JSON.class, false)
                .addMethod(DTOSpecifierJSON.specifyDecodeMethodFor(dtoClass));
        }

        if (dtoClass.isToBeEncodableAs(Format.JSON)) {
            root
                .addSuperinterface(WritableDTO.JSON.class, false)
                .addMethod(DTOSpecifierJSON.specifyEncodeMethodFor(dtoClass));
        }

        final var constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(ParameterSpec.builder(ClassName.bestGuess("Builder"), "builder")
                .addModifiers(Modifier.FINAL)
                .build());

        final var builder = TypeSpec.classBuilder("Builder")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        dtoClass.properties().forEach(property -> {
            final var name = property.defaultName();
            final var type = TypeName.get(property.typeMirror());
            final var field = FieldSpec.builder(type, name, Modifier.PRIVATE).build();

            root.addField(field);
            root.addMethod(MethodSpec.methodBuilder(name)
                .addJavadoc("{@inheritDoc}")
                .addJavadoc("isWritable: " + property.isWritable() + ", isReadable: " + property.isReadable())
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(property.isOptional()
                    ? ParameterizedTypeName.get(ClassName.get(Optional.class), type)
                    : type)
                .addCode(property.isOptional()
                    ? "return Optional.ofNullable(" + name + ");"
                    : "return " + name + ";")
                .build());

            builder.addField(field);
            builder.addMethod(MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(type, name, Modifier.FINAL).build())
                .returns(ClassName.bestGuess("Builder"))
                .addCode("this." + name + " = " + name + ";\n" +
                    "return this;")
                .build());

            if (property.isOptional() || property.typeMirror().getKind().isPrimitive()) {
                constructor.addStatement("this." + name + " = builder." + name);
            }
            else {
                constructor.addStatement("this." + name +
                        " = $T.requireNonNull(builder." + name +
                        ", \"Expected " + name + "\")",
                    Objects.class);
            }
        });

        return root
            .addMethod(constructor.build())
            .addType(builder
                .addMethod(MethodSpec.methodBuilder("build")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.bestGuess(dtoClass.name()))
                    .addCode("return new " + dtoClass.name() + "(this);")
                    .build())
                .build())
            .build();
    }
}
