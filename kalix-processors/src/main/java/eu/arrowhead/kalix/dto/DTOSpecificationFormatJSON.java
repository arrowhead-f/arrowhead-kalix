package eu.arrowhead.kalix.dto;

import com.squareup.javapoet.*;
import eu.arrowhead.kalix.dto.json.JSONToken;
import eu.arrowhead.kalix.dto.json.JSONWriter;
import eu.arrowhead.kalix.dto.types.*;
import eu.arrowhead.kalix.dto.util.ByteBufferPutBuilder;

import javax.lang.model.element.Modifier;
import java.nio.ByteBuffer;
import java.util.List;

public class DTOSpecificationFormatJSON implements DTOSpecificationFormat {
    private final ByteBufferPutBuilder putBuilder = new ByteBufferPutBuilder("target");

    private int level = 0;

    @Override
    public Format format() {
        return Format.JSON;
    }

    @Override
    public void implementFor(final DTOTarget target, final TypeSpec.Builder implementation) {
        if (target.interfaceType().isReadable(Format.JSON)) {
            putBuilder.clear();
            implementation.addSuperinterface(ReadableDTO.JSON.class);
            implementDecodeMethodFor(target, implementation);
        }
        if (target.interfaceType().isWritable(Format.JSON)) {
            putBuilder.clear();
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
        final var builder = MethodSpec.methodBuilder("encodeJSON")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addException(WriteException.class)
            .addParameter(ParameterSpec.builder(TypeName.get(ByteBuffer.class), "target")
                .addModifiers(Modifier.FINAL)
                .build());

        putBuilder.append('{');

        final var properties = target.properties();
        final var p1 = properties.size();
        for (var p0 = 0; p0 < p1; ++p0) {
            final var property = properties.get(p0);

            putBuilder
                .append('"')
                .append(property.nameFor(Format.JSON))
                .append("\":");

            encodeValue(property.type(), property.name(), builder);

            if (p0 + 1 != p1) {
                putBuilder.append(',');
            }
        }

        putBuilder.append('}').addPutIfNotEmpty(builder);

        implementation.addMethod(builder.build());
    }

    private void encodeValue(final DTOType type, final String name, final MethodSpec.Builder builder) {
        if (type instanceof DTOInterface) {
            encodeInterface(name, builder);
        }
        else if (type instanceof DTOMap) {
            encodeMap(type, name, builder);
        }
        else if (type instanceof DTOArrayOrList) {
            encodeArray(type, name, builder);
        }
        else if (type instanceof DTOEnum) {
            encodeEnum(name, builder);
        }
        else if (type instanceof DTOPrimitive) {
            encodePrimitive(name, builder);
        }
        else if (type instanceof DTOString) {
            encodeString(name, builder);
        }
        else {
            throw new IllegalStateException("Unexpected DTO type: "
                + type.name() + "(" + type + ")");
        }
    }

    private void encodeInterface(final String name, final MethodSpec.Builder builder) {
        putBuilder.addPutIfNotEmpty(builder);

        builder.addStatement("$N.encodeJSON(target)", name);
    }

    private void encodeMap(final DTOType type, final String name, final MethodSpec.Builder builder) {
        putBuilder.append('{').addPutIfNotEmpty(builder);

        final var map = (DTOMap) type;
        builder
            .addCode("{final var entrySet$L = $N.entrySet();\n", level, name)
            .addCode("final var size$1L = entrySet$1L.size();\n", level)
            .addCode("var i$L = 0;\n", level)
            .addCode("for (final var entry$1L : entrySet$1L) {\n", level);

        encodeValue(map.key(), "entry" + level + ".getKey()", builder);

        putBuilder.append(':');

        final var valueName = "entry" + level + ".getValue()";
        level += 1;
        encodeValue(map.value(), valueName, builder);
        level -= 1;

        putBuilder.addPutIfNotEmpty(builder);
        builder.addCode("if (++i$1L != size$1L) { target.put((byte) ','); }}}\n", level);

        putBuilder.append('}');
    }

    private void encodeArray(final DTOType type, final String name, final MethodSpec.Builder builder) {
        putBuilder.append('[').addPutIfNotEmpty(builder);

        final var arrayOrList = (DTOArrayOrList) type;
        if (arrayOrList instanceof DTOArray) {
            builder.addCode("{final var size$L = $N.length;\n", level, name);
        }
        else {
            builder.addCode("{final var size$L = $N.size();\n", level, name);
        }
        builder
            .addCode("var i$L = 0;\n", level)
            .addCode("for (final var item$1L : $2N) {\n", level, name);

        final var itemName = "item" + level;
        level += 1;
        encodeValue(arrayOrList.element(), itemName, builder);
        level -= 1;

        putBuilder.addPutIfNotEmpty(builder);
        builder.addCode("if (++i$1L != size$1L) { target.put((byte) ','); }}}\n", level);

        putBuilder.append(']');
    }

    private void encodeEnum(final String name, final MethodSpec.Builder builder) {
        putBuilder.append('"').addPutIfNotEmpty(builder);
        builder.addStatement("$T.writeTo($N.toString(), target)", JSONWriter.class, name);
        putBuilder.append('"');
    }

    private void encodePrimitive(final String name, final MethodSpec.Builder builder) {
        putBuilder.addPutIfNotEmpty(builder);
        builder.addStatement("$T.writeTo($N, target)", JSONWriter.class, name);
    }

    private void encodeString(final String name, final MethodSpec.Builder builder) {
        putBuilder.append('"').addPutIfNotEmpty(builder);
        builder.addStatement("$T.writeTo($N, target)", JSONWriter.class, name);
        putBuilder.append('"');
    }
}
