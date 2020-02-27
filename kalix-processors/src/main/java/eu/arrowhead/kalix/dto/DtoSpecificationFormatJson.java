package eu.arrowhead.kalix.dto;

import com.squareup.javapoet.*;
import eu.arrowhead.kalix.dto.json.*;
import eu.arrowhead.kalix.dto.types.*;
import eu.arrowhead.kalix.dto.util.ByteBufferPutCache;

import javax.lang.model.element.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

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
            .addStatement("return readJson(new $T(tokens, source))", JsonReadState.class)
            .build());

        final var builder = MethodSpec.methodBuilder("readJson")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(targetClassName)
            .addParameter(ParameterSpec.builder(TypeName.get(JsonReadState.class), "state")
                .addModifiers(Modifier.FINAL)
                .build())
            .addException(ReadException.class)
            .addCode("" +
                "final var source = state.source();\n" +
                "var token = state.next();\n" +
                "var error = \"\";\n");

        final var hasNumber = target.properties().stream().anyMatch(property -> property.descriptor().isNumber());
        if (hasNumber) {
            builder.addCode("error: try {\n");
        }
        else {
            builder.addCode("error: {\n");
        }

        builder.addCode("" +
                "if (token.type() != $1T.OBJECT) {\n" +
                "    error = \"Expected object\"; break error;\n" +
                "}\n" +
                "var nChildren = token.nChildren();\n" +
                "final var builder = new $2NBuilder();\n" +
                "while (nChildren > 0) {\n" +
                "    token = state.next();\n" +
                "    if (token.type() != $1T.STRING) {\n" +
                "        error = \"Expected string key\"; break error;\n" +
                "    }\n" +
                "    switch (token.readString(source)) {\n",
            JsonType.class, target.interfaceType().simpleName());

        for (final var property : target.properties()) {
            builder.addCode("" +
                    "    case $S: {\n",
                property.nameFor(Format.JSON));

            readValue(property.type(), "" +
                    "        final var value",
                builder);

            builder.addCode("" +
                    "        builder.$N(value);\n" +
                    "    } break;\n",
                property.name());
        }

        builder.addCode("" +
            "    }\n" +
            "nChildren -= 1;" +
            "}\n" +
            "return builder.build();\n");

        if (hasNumber) {
            builder.addCode("" +
                    "} catch (final $1T exception) { error = exception.getMessage(); }\n",
                NumberFormatException.class);
        }
        else {
            builder.addCode("}\n");
        }

        builder.addCode("" +
                "throw new $1T($2T.JSON, error, token.readString(source), token.begin());\n",
            ReadException.class, Format.class);

        implementation.addMethod(builder.build());
    }

    private void readValue(final DtoType type, final String lhs, final MethodSpec.Builder builder) {
        final var descriptor = type.descriptor();
        switch (descriptor) {
        case ARRAY:
            readArray((DtoArray) type, lhs, builder);
            break;

        case BOOLEAN_BOXED:
        case BOOLEAN_UNBOXED:
            readBoolean(lhs, builder);
            break;

        case BYTE_BOXED:
        case BYTE_UNBOXED:
            readByte(lhs, builder);
            break;

        case CHARACTER_BOXED:
        case CHARACTER_UNBOXED:
            throw characterTypesNotSupportedException();

        case DOUBLE_BOXED:
        case DOUBLE_UNBOXED:
            readDouble(lhs, builder);
            break;

        case ENUM:
            readEnum(type, lhs, builder);
            break;

        case FLOAT_BOXED:
        case FLOAT_UNBOXED:
            readFloat(lhs, builder);
            break;

        case INTEGER_BOXED:
        case INTEGER_UNBOXED:
            readInteger(lhs, builder);
            break;

        case INTERFACE:
            readInterface((DtoInterface) type, lhs, builder);
            break;

        case LIST:
            readList((DtoList) type, lhs, builder);
            break;

        case LONG_BOXED:
        case LONG_UNBOXED:
            readLong(lhs, builder);
            break;

        case MAP:
            readMap((DtoMap) type, lhs, builder);
            break;

        case SHORT_BOXED:
        case SHORT_UNBOXED:
            readShort(lhs, builder);
            break;

        case STRING:
            readString(lhs, builder);
            break;

        default:
            throw new IllegalStateException("Unexpected type: " + type);
        }
    }

    private void readArray(final DtoArray type, final String lhs, final MethodSpec.Builder builder) {
        final var element = type.element();

        builder.addCode("" +
                "token = state.next();\n" +
                "if (token.type() != $1T.ARRAY) {\n" +
                "    error = \"Expected array\"; break error;\n" +
                "}\n" +
                "final var items$2L = new $3T[token.nChildren()];\n" +
                "for (var i$2L = 0; i$2L < items$2L.length; ++i$2L) {\n",
            JsonType.class, level, element.asTypeMirror());

        final var itemLhs = "items" + level + "[i" + level + "]";

        level += 1;
        readValue(element, itemLhs, builder);
        level -= 1;

        builder.addCode("" +
                "}\n" +
                lhs + " = items$L;\n",
            level);
    }

    private void readBoolean(final String lhs, final MethodSpec.Builder builder) {
        builder.addCode("" +
                "token = state.next();\n" +
                "if (token.type() != $1T.TRUE && token.type() != $1T.FALSE) {\n" +
                "    error = \"Expected true or false\"; break error;\n" +
                "}\n" +
                lhs + " = token.getBoolean();",
            JsonType.class);
    }

    private void readByte(final String lhs, final MethodSpec.Builder builder) {
        builder.addCode("" +
                "token = state.next();\n" +
                "if (token.type() != $1T.NUMBER) {\n" +
                "    error = \"Expected number\"; break error;\n" +
                "}\n" +
                lhs + " = token.readByte(source);\n",
            JsonType.class);
    }

    private void readDouble(final String lhs, final MethodSpec.Builder builder) {
        builder.addCode("" +
                "token = state.next();\n" +
                "if (token.type() != $1T.NUMBER) {\n" +
                "    error = \"Expected number\"; break error;\n" +
                "}\n" +
                lhs + " = token.readDouble(source);\n",
            JsonType.class);
    }

    private void readEnum(final DtoType type, final String lhs, final MethodSpec.Builder builder) {
        builder.addCode("" +
                "token = state.next();\n" +
                "if (token.type() != $1T.STRING) {\n" +
                "    error = \"Expected string\"; break error;\n" +
                "}\n" +
                lhs + " = $2T.valueOf(token.readString(source));\n",
            JsonType.class, type.asTypeMirror());
    }

    private void readFloat(final String lhs, final MethodSpec.Builder builder) {
        builder.addCode("" +
                "token = state.next();\n" +
                "if (token.type() != $1T.NUMBER) {\n" +
                "    error = \"Expected number\"; break error;\n" +
                "}\n" +
                lhs + " = token.readFloat(source);\n",
            JsonType.class);
    }

    private void readInteger(final String lhs, final MethodSpec.Builder builder) {
        builder.addCode("" +
                "token = state.next();\n" +
                "if (token.type() != $1T.NUMBER) {\n" +
                "    error = \"Expected number\"; break error;\n" +
                "}\n" +
                lhs + " = token.readInteger(source);\n",
            JsonType.class);
    }

    private void readInterface(final DtoInterface type, final String lhs, final MethodSpec.Builder builder) {
        final var className = ClassName.bestGuess(type.targetSimpleName());
        builder.addStatement(lhs + " = $T.readJson(state)", className);
    }

    private void readList(final DtoList type, final String lhs, final MethodSpec.Builder builder) {
        final var element = type.element();

        builder.addCode("" +
                "token = state.next();\n" +
                "if (token.type() != $1T.ARRAY) {\n" +
                "    error = \"Expected array\"; break error;\n" +
                "}\n" +
                "final var nChildren$2L = token.nChildren();\n" +
                "final var items$2L = new $3T<$4T>(nChildren$2L);\n" +
                "for (var i$2L = 0; i$2L < nChildren$2L; ++i$2L) {\n",
            JsonType.class, level, ArrayList.class, element.asTypeMirror());

        final var itemLhs = "final var item" + level;

        level += 1;
        readValue(element, itemLhs, builder);
        level -= 1;

        builder.addCode("" +
                "items$1L.add(item$1L);\n" +
                "}\n" +
                lhs + " = items$1L;\n",
            level);
    }

    private void readLong(final String lhs, final MethodSpec.Builder builder) {
        builder.addCode("" +
                "token = state.next();\n" +
                "if (token.type() != $1T.NUMBER) {\n" +
                "    error = \"Expected number\"; break error;\n" +
                "}\n" +
                lhs + " = token.readLong(source);\n",
            JsonType.class);
    }

    private void readMap(final DtoMap type, final String lhs, final MethodSpec.Builder builder) {
        final var key = type.key();
        final var value = type.value();

        builder.addCode("" +
                "token = state.next();\n" +
                "if (token.type() != $1T.OBJECT) {\n" +
                "    error = \"Expected object\"; break error;\n" +
                "}\n" +
                "final var nChildren$2L = token.nChildren();\n" +
                "final var entries$2L = new $3T<$4T, $5T>(nChildren$2L);\n" +
                "for (var i$2L = 0; i$2L < nChildren$2L; ++i$2L) {\n",
            JsonType.class, level, HashMap.class, key.asTypeMirror(), value.asTypeMirror());

        final var keyLhs = "final var key" + level;
        final var valueLhs = "final var value" + level;

        level += 1;
        readValue(key, keyLhs, builder);
        readValue(value, valueLhs, builder);
        level -= 1;

        builder.addCode("" +
                "entries$1L.put(key$1L, value$1L);\n" +
                "}\n" +
                lhs + " = entries$1L;\n",
            level);
    }

    private void readShort(final String lhs, final MethodSpec.Builder builder) {
        builder.addCode("" +
                "token = state.next();\n" +
                "if (token.type() != $1T.NUMBER) {\n" +
                "    error = \"Expected number\"; break error;\n" +
                "}\n" +
                lhs + " = token.readShort(source);\n",
            JsonType.class);
    }

    private void readString(final String lhs, final MethodSpec.Builder builder) {
        builder.addCode("" +
                "token = state.next();\n" +
                "if (token.type() != $1T.STRING) {\n" +
                "    error = \"Expected number\"; break error;\n" +
                "}\n" +
                lhs + " = token.readString(source);\n",
            JsonType.class);
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

                writeValue(property.type(), property.name(), builder);

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

    private void writeValue(final DtoType type, final String name, final MethodSpec.Builder builder) {
        final var descriptor = type.descriptor();
        switch (descriptor) {
        case ARRAY:
        case LIST:
            writeArray(type, name, builder);
            break;

        case CHARACTER_BOXED:
        case CHARACTER_UNBOXED:
            throw characterTypesNotSupportedException();

        case ENUM:
            writeEnum(name, builder);
            break;

        case INTERFACE:
            writeInterface(name, builder);
            break;

        case MAP:
            writeMap(type, name, builder);
            break;

        case STRING:
            writeString(name, builder);
            break;

        default:
            if (descriptor.isPrimitive()) {
                writePrimitive(name, builder);
            }
            else {
                throw new IllegalStateException("Unexpected type: " + type);
            }
        }
    }

    private void writeArray(final DtoType type, final String name, final MethodSpec.Builder builder) {
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
        writeValue(((DtoArrayOrList) type).element(), itemName, builder);
        level -= 1;

        putCache.addPutIfNotEmpty(builder);
        builder.addCode("" +
            "if (++i$1L != size$1L) {" +
            "target.put((byte) ',');" +
            "}}}\n", level);

        putCache.append(']');
    }

    private void writeEnum(final String name, final MethodSpec.Builder builder) {
        putCache.append('"').addPut(builder);
        builder.addStatement("$T.write($N.toString(), target)", JsonWriter.class, name);
        putCache.append('"');
    }

    private void writeInterface(final String name, final MethodSpec.Builder builder) {
        putCache.addPutIfNotEmpty(builder);

        builder.addStatement("$N.writeJson(target)", name);
    }

    private void writeMap(final DtoType type, final String name, final MethodSpec.Builder builder) {
        putCache.append('{').addPutIfNotEmpty(builder);

        final var map = (DtoMap) type;
        builder.addCode("{" +
                "final var entrySet$1L = $2N.entrySet();\n" +
                "final var size$1L = entrySet$1L.size();\n" +
                "var i$1L = 0;\n" +
                "for (final var entry$1L : entrySet$1L) {\n",
            level, name);

        writeValue(map.key(), "entry" + level + ".getKey()", builder);

        putCache.append(':');

        final var valueName = "entry" + level + ".getValue()";
        level += 1;
        writeValue(map.value(), valueName, builder);
        level -= 1;

        putCache.addPutIfNotEmpty(builder);
        builder.addCode("" +
                "if (++i$1L != size$1L) {" +
                "target.put((byte) ',');" +
                "}}}\n",
            level);

        putCache.append('}');
    }

    private void writeString(final String name, final MethodSpec.Builder builder) {
        putCache.append('"').addPut(builder);
        builder.addStatement("$T.write($N, target)", JsonWriter.class, name);
        putCache.append('"');
    }

    private void writePrimitive(final String name, final MethodSpec.Builder builder) {
        putCache.addPutIfNotEmpty(builder);
        builder.addStatement("$T.write($N, target)", JsonWriter.class, name);
    }

    private static IllegalStateException characterTypesNotSupportedException() {
        return new IllegalStateException("The char and Character types " +
            "cannot be represented as JSON; either change the type or " +
            "remove Format.JSON from the array of formats provided to " +
            "the @Readable/@Writable annotation");
    }
}
