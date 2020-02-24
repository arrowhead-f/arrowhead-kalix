package eu.arrowhead.kalix.processors.io;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.nio.ByteBuffer;

public final class DTOTargetJson {
    private DTOTargetJson() {}

    public static void specifyJsonDecoder(final TypeElement ie, final TypeSpec.Builder ts) {
        final var methodSpec = MethodSpec.methodBuilder("decodeJson")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(TypeName.get(ie.asType()))
            .addParameter(ParameterSpec.builder(TypeName.get(ByteBuffer.class), "buffer")
                .addModifiers(Modifier.FINAL)
                .build())
            .addCode("return null; // TODO")
            .build();

        ts.addMethod(methodSpec);
    }

    public static void specifyJsonEncoder(final TypeElement ie, final TypeSpec.Builder ts) {
        final var methodSpec = MethodSpec.methodBuilder("encodeJson")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(TypeName.get(ByteBuffer.class), "buffer")
                .addModifiers(Modifier.FINAL)
                .build())
            .addCode("// TODO")
            .build();

        ts.addMethod(methodSpec);
    }
}
