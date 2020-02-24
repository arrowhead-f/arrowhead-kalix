package eu.arrowhead.kalix.processors.io;

import com.squareup.javapoet.*;
import eu.arrowhead.kalix.util.io.DTO;

import javax.lang.model.element.Modifier;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class DTOSpecifierJSON {
    private DTOSpecifierJSON() {}

    public static MethodSpec specifyDecodeMethodFor(final DTOClass dtoClass) {
        return MethodSpec.methodBuilder("decodeJson")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(TypeName.get(dtoClass.origin().asType()))
            .addParameter(ParameterSpec.builder(TypeName.get(ByteBuffer.class), "source")
                .addModifiers(Modifier.FINAL)
                .build())
            .addCode("return null; // TODO")
            .build();
    }

    public static MethodSpec specifyDecoderLookupFor(final Set<DTOClass> dtoClasses) {
        return MethodSpec.methodBuilder("getJsonDecoderFor")
            .addJavadoc("Gets function useful for decoding the contents of " +
                "some {@link ByteBuffer} into instances of X, if any.")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addTypeVariable(TypeVariableName.get("T", DTO.DecodableJSON.class))
            .returns(ParameterizedTypeName.get(
                ClassName.get(Optional.class),
                ParameterizedTypeName.get(
                    ClassName.get(Function.class),
                    TypeName.get(ByteBuffer.class),
                    TypeVariableName.get("T"))))
            .addParameter(ParameterSpec.builder(
                ParameterizedTypeName.get(
                    ClassName.get(Class.class),
                    TypeVariableName.get("T")),
                "encodableClass",
                Modifier.FINAL)
                .build())
            .addCode("return Optional.empty(); // TODO")
            .build();
    }

    public static MethodSpec specifyEncodeMethodFor(final DTOClass dtoClass) {
        return MethodSpec.methodBuilder("encodeJson")
            .addJavadoc("{@inheritDoc}")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(TypeName.get(ByteBuffer.class), "target")
                .addModifiers(Modifier.FINAL)
                .build())
            .addCode("// TODO")
            .build();
    }
}
