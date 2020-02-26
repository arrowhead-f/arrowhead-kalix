package eu.arrowhead.kalix.dto;

import com.squareup.javapoet.*;
import eu.arrowhead.kalix.dto.json.JSONToken;
import eu.arrowhead.kalix.dto.json.JSONWriter;

import javax.lang.model.element.Modifier;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

public class DTOSpecificationFormatJSON implements DTOSpecificationFormat {
    private static final String packageName = JSONWriter.class.getPackageName();
    private final TypeSpec.Builder utilityBuilder = TypeSpec.classBuilder("JSONUtils")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .build());

    @Override
    public Format format() {
        return Format.JSON;
    }

    @Override
    public void implementFor(final DTOTarget target, final TypeSpec.Builder implementation) throws DTOException {
        if (target.interfaceType().isReadable(Format.JSON)) {
            implementation.addSuperinterface(ReadableDTO.JSON.class);
            implementDecodeMethodFor(target, implementation);
        }
        if (target.interfaceType().isWritable(Format.JSON)) {
            implementation.addSuperinterface(WritableDTO.JSON.class);
            implementEncodeMethodFor(target, implementation);
        }
    }

    private void implementDecodeMethodFor(final DTOTarget target, final TypeSpec.Builder implementation) {
        final var decodeJSON = MethodSpec.methodBuilder("decodeJSON")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(ClassName.bestGuess(target.simpleName()))
            .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(List.class, JSONToken.class), "tokens")
                .addModifiers(Modifier.FINAL)
                .build())
            .addParameter(ParameterSpec.builder(ByteBuffer.class, "source")
                .addModifiers(Modifier.FINAL)
                .build())
            .addException(ReadException.class)
            .addCode("return null; // TODO")
            .build();

        implementation.addMethod(decodeJSON);
    }

    private void implementEncodeMethodFor(final DTOTarget target, final TypeSpec.Builder implementation) {
        final var encodeJSON = MethodSpec.methodBuilder("encodeJSON")
            .addJavadoc("{@inheritDoc}")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(TypeName.get(ByteBuffer.class), "target")
                .addModifiers(Modifier.FINAL)
                .build())
            .addCode("// TODO")
            .build();

        implementation.addMethod(encodeJSON);
    }

    @Override
    public Optional<DTOUtilitySpecification> utilitySpecification() {
        return Optional.of(new DTOUtilitySpecification(packageName, utilityBuilder.build()));
    }
}
