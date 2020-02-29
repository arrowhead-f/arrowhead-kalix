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
            .addParameter(ByteBuffer.class, "source", Modifier.FINAL)
            .addException(ReadException.class)
            .addStatement("return readJson($T.tokenize(source))", JsonReader.class)
            .build());

        final var builder = MethodSpec.methodBuilder("readJson")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(targetClassName)
            .addParameter(JsonTokenReader.class, "reader", Modifier.FINAL)
            .addException(ReadException.class)
            .addStatement("final var source = reader.source()")
            .addStatement("var token = reader.next()")
            .addStatement("var error = \"\"");

        final var hasNumber = target.properties().stream().anyMatch(property -> property.descriptor().isNumber());
        if (hasNumber) {
            builder.beginControlFlow("error: try");
        }
        else {
            builder.beginControlFlow("error:");
        }

        final var builderName = target.interfaceType().simpleName() + "Builder";
        builder
            .beginControlFlow("if (token.type() != $T.OBJECT)", JsonType.class)
            .addStatement("error = \"Expected object\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement("final var builder = new $N()", builderName)
            .beginControlFlow("for (var n = token.nChildren(); n-- != 0; )")
            .beginControlFlow("switch (reader.next().readString(source))");

        for (final var property : target.properties()) {
            builder
                .beginControlFlow("case $S:", property.nameFor(Format.JSON));

            readValue(property.type(), "final var value", builder);

            builder
                .addStatement("builder.$N(value)", property.name())
                .endControlFlow("break");
        }

        builder
            .endControlFlow()
            .endControlFlow()
            .addStatement("return builder.build()");

        if (hasNumber) {
            builder
                .endControlFlow()
                .beginControlFlow("catch (final $1T exception) ", NumberFormatException.class)
                .addStatement("error = exception.getMessage()")
                .endControlFlow();
        }
        else {
            builder.endControlFlow();
        }

        builder.addCode("throw new $1T($2T.JSON, error, token.readString(source), token.begin());\n",
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

        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.ARRAY)", JsonType.class)
            .addStatement("error = \"Expected array\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement("final var items$L = new $T[token.nChildren()]", level, element.asTypeMirror())
            .beginControlFlow("for (var i$1L = 0; i$1L < items$1L.length; ++i$1L)", level);

        final var itemLhs = "items" + level + "[i" + level + "]";

        level += 1;
        readValue(element, itemLhs, builder);
        level -= 1;

        builder
            .endControlFlow()
            .addStatement(lhs + " = items$L", level);
    }

    private void readBoolean(final String lhs, final MethodSpec.Builder builder) {
        builder
            .beginControlFlow("switch (reader.next().type())")
            .addStatement("case TRUE: " + lhs + " = true; break")
            .addStatement("case FALSE: " + lhs + " = false; break")
            .beginControlFlow("default:")
            .addStatement("error = \"Expected true or false\"")
            .addStatement("break error")
            .endControlFlow()
            .endControlFlow();
    }

    private void readByte(final String lhs, final MethodSpec.Builder builder) {
        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.NUMBER)", JsonType.class)
            .addStatement("error = \"Expected number\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement(lhs + " = token.readByte(source)");
    }

    private void readDouble(final String lhs, final MethodSpec.Builder builder) {
        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.NUMBER)", JsonType.class)
            .addStatement("error = \"Expected number\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement(lhs + " = token.readDouble(source)");
    }

    private void readEnum(final DtoType type, final String lhs, final MethodSpec.Builder builder) {
        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.STRING)", JsonType.class)
            .addStatement("error = \"Expected number\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement(lhs + " = $T.valueOf(token.readString(source))", type.asTypeMirror());
    }

    private void readFloat(final String lhs, final MethodSpec.Builder builder) {
        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.NUMBER)", JsonType.class)
            .addStatement("error = \"Expected number\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement(lhs + " = token.readFloat(source)");
    }

    private void readInteger(final String lhs, final MethodSpec.Builder builder) {
        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.NUMBER)", JsonType.class)
            .addStatement("error = \"Expected number\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement(lhs + " = token.readInteger(source)");
    }

    private void readInterface(final DtoInterface type, final String lhs, final MethodSpec.Builder builder) {
        final var className = ClassName.bestGuess(type.targetSimpleName());
        builder.addStatement(lhs + " = $T.readJson(reader)", className);
    }

    private void readList(final DtoList type, final String lhs, final MethodSpec.Builder builder) {
        final var element = type.element();

        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.ARRAY)", JsonType.class)
            .addStatement("error = \"Expected array\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement("var n$L = token.nChildren()", level)
            .addStatement("final var items$1L = new $2T<$3T>(n$1L)", level, ArrayList.class, element.asTypeMirror())
            .beginControlFlow("while (n$L-- != 0)", level);

        final var itemLhs = "final var item" + level;

        level += 1;
        readValue(element, itemLhs, builder);
        level -= 1;

        builder
            .addStatement("items$1L.add(item$1L)", level)
            .endControlFlow()
            .addStatement(lhs + " = items$L", level);
    }

    private void readLong(final String lhs, final MethodSpec.Builder builder) {
        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.NUMBER)", JsonType.class)
            .addStatement("error = \"Expected number\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement(lhs + " = token.readLong(source)");
    }

    private void readMap(final DtoMap type, final String lhs, final MethodSpec.Builder builder) {
        final var key = type.key();
        final var value = type.value();

        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.OBJECT)", JsonType.class)
            .addStatement("error = \"Expected array\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement("var n$L = token.nChildren()", level)
            .addStatement("final var entries$1L = new $2T<$3T, $4T>(n$1L)",
                level, HashMap.class, key.asTypeMirror(), value.asTypeMirror())
            .beginControlFlow("while (n$L-- != 0)", level)
            .addStatement("final var key$L = reader.next().readString(source)", level);

        final var valueLhs = "final var value" + level;

        level += 1;
        readValue(value, valueLhs, builder);
        level -= 1;

        builder
            .addStatement("entries$1L.put(key$1L, value$1L)", level)
            .endControlFlow()
            .addStatement(lhs + " = entries$L", level);
    }

    private void readShort(final String lhs, final MethodSpec.Builder builder) {
        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.NUMBER)", JsonType.class)
            .addStatement("error = \"Expected number\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement(lhs + " = token.readShort(source)");
    }

    private void readString(final String lhs, final MethodSpec.Builder builder) {
        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.STRING)", JsonType.class)
            .addStatement("error = \"Expected number\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement(lhs + " = token.readString(source)");
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
            final var isOptional = property.isOptional();
            try {
                if (p0 != 0) {
                    if (isOptional) {
                        putCache.addPutIfNotEmpty(builder);
                        builder.beginControlFlow("if ($N != null)", property.name());
                    }
                    putCache.append(',');
                }

                putCache
                    .append('"')
                    .append(property.nameFor(Format.JSON))
                    .append("\":");

                writeValue(property.type(), property.name(), builder);

                if (isOptional) {
                    putCache.addPutIfNotEmpty(builder);
                    builder.endControlFlow();
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

        builder
            .beginControlFlow("")
            .addStatement("final var size$L = $N.$N",
                level, name, type.descriptor() == DtoDescriptor.ARRAY
                    ? "length"
                    : "size()")
            .addStatement("var i$L = 0", level)
            .beginControlFlow("for (final var item$L : $N)", level, name)
            .beginControlFlow("if (i$L++ != 0)", level)
            .addStatement("target.put((byte) ',')")
            .endControlFlow();

        final var itemName = "item" + level;
        level += 1;
        writeValue(((DtoArrayOrList) type).element(), itemName, builder);
        level -= 1;

        putCache.addPutIfNotEmpty(builder);
        builder
            .endControlFlow()
            .endControlFlow();

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
        builder
            .beginControlFlow("")
            .addStatement("final var entrySet$L = $N.entrySet()", level, name)
            .addStatement("final var size$1L = entrySet$1L.size()", level)
            .addStatement("var i$L = 0", level)
            .beginControlFlow("for (final var entry$1L : entrySet$1L)", level)
            .beginControlFlow("if (i$L++ != 0)", level)
            .addStatement("target.put((byte) ',')")
            .endControlFlow();

        writeValue(map.key(), "entry" + level + ".getKey()", builder);

        putCache.append(':');

        final var valueName = "entry" + level + ".getValue()";
        level += 1;
        writeValue(map.value(), valueName, builder);
        level -= 1;

        putCache.addPutIfNotEmpty(builder);
        builder
            .endControlFlow()
            .endControlFlow();

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
            "the @Readable/@Writable annotation(s)");
    }
}
