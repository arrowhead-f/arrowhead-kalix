package eu.arrowhead.kalix.dto;

import com.squareup.javapoet.*;
import eu.arrowhead.kalix.dto.json.*;
import eu.arrowhead.kalix.dto.types.*;
import eu.arrowhead.kalix.dto.util.ByteBufferPutCache;

import javax.lang.model.element.Modifier;
import java.nio.ByteBuffer;
import java.util.List;

public class DtoSpecificationFormatJson implements DtoSpecificationFormat {
    private final ByteBufferPutCache putCache = new ByteBufferPutCache("target");

    private int level = 0;

    @Override
    public Format format() {
        return Format.JSON;
    }

    @Override
    public void implementFor(final DtoTarget target, final TypeSpec.Builder implementation) throws DtoException {
        if (target.interfaceType().isReadable(Format.JSON)) {
            implementReadMethodsFor(target, implementation);
        }
        if (target.interfaceType().isWritable(Format.JSON)) {
            implementation.addSuperinterface(JsonWritable.class);
            implementWriteMethodFor(target, implementation);
        }
    }

    private void implementReadMethodsFor(final DtoTarget target, final TypeSpec.Builder implementation) {
        final var targetClassName = ClassName.bestGuess(target.simpleName());

        implementation.addMethod(MethodSpec.methodBuilder("readJson")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(targetClassName)
            .addParameter(ParameterSpec.builder(TypeName.get(ByteBuffer.class), "source")
                .addModifiers(Modifier.FINAL)
                .build())
            .addException(ReadException.class)
            .addStatement("final var tokens = $T.tokenize(source)", JsonReader.class)
            .addStatement("return readJson(tokens, 0, source)")
            .build());

        final var builder = MethodSpec.methodBuilder("readJson")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(targetClassName)
            .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(List.class, JsonToken.class), "tokens")
                .addModifiers(Modifier.FINAL)
                .build())
            .addParameter(ParameterSpec.builder(TypeName.INT, "offset").build())
            .addParameter(ParameterSpec.builder(ByteBuffer.class, "source")
                .addModifiers(Modifier.FINAL)
                .build())
            .addException(ReadException.class)
            .addCode("" +
                    "if (tokens.size() >= offset) {\n" +
                    "    throw new $1T($2T.JSON, \"Expected object\", \"\", 0);\n" +
                    "}\n" +
                    "var token = tokens.get(offset++);\n" +
                    "var error = \"\";\n" +
                    "error: {\n" +
                    "if (token.type() != $3T.OBJECT) {\n" +
                    "    error = \"Expected object\"; break error;\n" +
                    "}\n" +
                    "var nChildren = token.nChildren();\n" +
                    "final var builder = new $4NBuilder();\n" +
                    "while (nChildren > 0 && offset < tokens.size()) {\n" +
                    "    token = tokens.get(offset++);\n" +
                    "    if (token.type() != $3T.STRING) {\n" +
                    "        error = \"Expected string key\"; break error;\n" +
                    "    }\n" +
                    "    final var name = token.getStringFrom(source);\n" +
                    "    switch (name) {\n",
                ReadException.class, Format.class, JsonType.class,
                target.interfaceType().simpleName());

        for (final var property : target.properties()) {
            builder.addCode("    case $S:\n", property.nameFor(Format.JSON));

            builder.addCode("        break;\n");
        }

        builder.addCode("" +
                "    default: /* Log debug? */ break;\n" +
                "    }\n" +
                "}\n" +
                "return builder.build();\n" +
                "}\n" +
                "throw new $1T($2T.JSON, error, token.getStringFrom(source), token.begin());\n",
            ReadException.class, Format.class);

        implementation.addMethod(builder.build());
    }

    private void implementWriteMethodFor(final DtoTarget target, final TypeSpec.Builder implementation) throws DtoException {
        final var builder = MethodSpec.methodBuilder("writeJson")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addException(WriteException.class)
            .addParameter(ParameterSpec.builder(TypeName.get(ByteBuffer.class), "target")
                .addModifiers(Modifier.FINAL)
                .build());

        putCache.clear();
        putCache.append('{');

        final var properties = target.properties();
        final var p1 = properties.size();
        for (var p0 = 0; p0 < p1; ++p0) {
            final var property = properties.get(p0);
            try {
                if (property.isOptional()) {
                    putCache.addPutIfNotEmpty(builder);
                    builder.addCode("if ($N != null) {\n", property.name());
                }

                putCache
                    .append('"')
                    .append(property.nameFor(Format.JSON))
                    .append("\":");

                encodeValue(property.type(), property.name(), builder);

                if (p0 + 1 != p1) {
                    putCache.append(',');
                }

                if (property.isOptional()) {
                    putCache.addPutIfNotEmpty(builder);
                    builder.addCode("}\n");
                }
            }
            catch (final IllegalStateException exception) {
                throw new DtoException(property.parentElement(), exception.getMessage());
            }
        }

        putCache.append('}').addPutIfNotEmpty(builder);

        implementation.addMethod(builder.build());
    }

    private void encodeValue(final DtoType type, final String name, final MethodSpec.Builder builder) {
        final var descriptor = type.descriptor();
        switch (descriptor) {
        case ARRAY:
        case LIST:
            encodeArray(type, name, builder);
            break;

        case ENUM:
            encodeEnum(name, builder);
            break;

        case INTERFACE:
            encodeInterface(name, builder);
            break;

        case MAP:
            encodeMap(type, name, builder);
            break;

        case STRING:
            encodeString(name, builder);
            break;

        default:
            if (descriptor.isPrimitive()) {
                encodePrimitive(type, name, builder);
            }
            else {
                throw new IllegalStateException("Unexpected type: " + type);
            }
        }
    }

    private void encodeInterface(final String name, final MethodSpec.Builder builder) {
        putCache.addPutIfNotEmpty(builder);

        builder.addStatement("$N.writeJson(target)", name);
    }

    private void encodeMap(final DtoType type, final String name, final MethodSpec.Builder builder) {
        putCache.append('{').addPutIfNotEmpty(builder);

        final var map = (DtoMap) type;
        builder.addCode("{" +
                "final var entrySet$1L = $2N.entrySet();\n" +
                "final var size$1L = entrySet$1L.size();\n" +
                "var i$1L = 0;\n" +
                "for (final var entry$1L : entrySet$1L) {\n",
            level, name);

        encodeValue(map.key(), "entry" + level + ".getKey()", builder);

        putCache.append(':');

        final var valueName = "entry" + level + ".getValue()";
        level += 1;
        encodeValue(map.value(), valueName, builder);
        level -= 1;

        putCache.addPutIfNotEmpty(builder);
        builder.addCode("" +
                "if (++i$1L != size$1L) {" +
                "target.put((byte) ',');" +
                "}}}\n",
            level);

        putCache.append('}');
    }

    private void encodeArray(final DtoType type, final String name, final MethodSpec.Builder builder) {
        putCache.append('[').addPut(builder);

        if (type.descriptor() == DtoDescriptor.ARRAY) {
            builder.addCode("{final var size$L = $N.length;\n", level, name);
        }
        else {
            builder.addCode("{final var size$L = $N.size();\n", level, name);
        }
        builder.addCode("" +
                "var i$1L = 0;\n" +
                "for (final var item$1L : $2N) {\n",
            level, name);

        final var itemName = "item" + level;
        level += 1;
        encodeValue(((DtoArrayOrList) type).element(), itemName, builder);
        level -= 1;

        putCache.addPutIfNotEmpty(builder);
        builder.addCode("" +
            "if (++i$1L != size$1L) {" +
            "target.put((byte) ',');" +
            "}}}\n", level);

        putCache.append(']');
    }

    private void encodeEnum(final String name, final MethodSpec.Builder builder) {
        putCache.append('"').addPut(builder);
        builder.addStatement("$T.writeTo($N.toString(), target)", JsonWriter.class, name);
        putCache.append('"');
    }

    private void encodePrimitive(final DtoType type, final String name, final MethodSpec.Builder builder) {
        if (type.descriptor().isCharacter()) {
            throw new IllegalStateException("The char and Characters types " +
                "cannot be represented as JSON; either change the type or " +
                "remove Format.JSON from the array of formats provided to " +
                "the @Readable annotation");
        }
        putCache.addPutIfNotEmpty(builder);
        builder.addStatement("$T.writeTo($N, target)", JsonWriter.class, name);
    }

    private void encodeString(final String name, final MethodSpec.Builder builder) {
        putCache.append('"').addPut(builder);
        builder.addStatement("$T.writeTo($N, target)", JsonWriter.class, name);
        putCache.append('"');
    }
}
