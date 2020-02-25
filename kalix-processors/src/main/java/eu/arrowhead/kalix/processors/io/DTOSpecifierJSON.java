package eu.arrowhead.kalix.processors.io;

import com.squareup.javapoet.*;
import eu.arrowhead.kalix.dto.ReadException;
import eu.arrowhead.kalix.dto.json.JSONToken;

import javax.lang.model.element.Modifier;
import java.nio.ByteBuffer;
import java.util.List;

public class DTOSpecifierJSON {
    private DTOSpecifierJSON() {}

    public static MethodSpec specifyDecodeMethodFor(final DTOClass dtoClass) {
        return MethodSpec.methodBuilder("decodeJSON")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(TypeName.get(dtoClass.origin().asType()))
            .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(List.class, JSONToken.class), "tokens")
                .addModifiers(Modifier.FINAL)
                .build())
            .addParameter(ParameterSpec.builder(ByteBuffer.class, "source")
                .addModifiers(Modifier.FINAL)
                .build())
            .addException(ReadException.class)
            .addCode("return null; // TODO")
            .build();
    }

    public static MethodSpec specifyEncodeMethodFor(final DTOClass dtoClass) {
        return MethodSpec.methodBuilder("encodeJSON")
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
