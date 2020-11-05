package se.arkalix.dto;

import com.squareup.javapoet.*;
import se.arkalix.dto.types.*;
import se.arkalix.dto.util.BinaryWriterWriteCache;
import se.arkalix.dto.util.Expander;
import se.arkalix.encoding.DecoderReadUnexpectedToken;
import se.arkalix.encoding.Encoding;
import se.arkalix.encoding.binary.BinaryReader;
import se.arkalix.encoding.binary.BinaryWriter;
import se.arkalix.encoding.json.JsonType;
import se.arkalix.encoding.json._internal.JsonTokenBuffer;
import se.arkalix.encoding.json._internal.JsonTokenizer;
import se.arkalix.encoding.json._internal.JsonWrite;
import se.arkalix.util.annotation.Internal;

import javax.lang.model.element.Modifier;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DtoImplementerJson implements DtoImplementer {
    private final BinaryWriterWriteCache writeCache = new BinaryWriterWriteCache("writer");

    private int level = 0;

    @Override
    public String encoding() {
        return DtoEncoding.JSON;
    }

    @Override
    public void implementFor(final DtoTarget target, final TypeSpec.Builder implementation) throws DtoException {
        if (target.interfaceType().isReadable(DtoEncoding.JSON)) {
            implementReadMethodsFor(target, implementation);
        }
        if (target.interfaceType().isWritable(DtoEncoding.JSON)) {
            implementWriteMethodFor(target, implementation);
        }
    }

    private void implementReadMethodsFor(final DtoTarget target, final TypeSpec.Builder implementation)
        throws DtoException {
        final var dataTypeName = target.dataTypeName();
        final var properties = target.properties();

        implementation.addMethod(MethodSpec.methodBuilder("readJson")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(dataTypeName)
            .addParameter(BinaryReader.class, "reader", Modifier.FINAL)
            .addStatement("return readJson($T.tokenize(reader))", JsonTokenizer.class)
            .build());

        final var builder = MethodSpec.methodBuilder("readJson")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(dataTypeName)
            .addParameter(JsonTokenBuffer.class, "buffer", Modifier.FINAL)
            .addAnnotation(Internal.class)
            .addStatement("final var reader = buffer.reader()")
            .addStatement("var token = buffer.next()")
            .addStatement("var type = ($T) null", JsonType.class)
            .addStatement("var errorMessage = \"\"")
            .addStatement("var errorCause = ($T) null", Throwable.class)
            .addStatement("var n = -1");

        var hasEnum = properties.stream().anyMatch(property -> property.descriptor() == DtoDescriptor.ENUM);
        var hasInterface = properties.stream().anyMatch(property -> property.descriptor() == DtoDescriptor.INTERFACE);
        var hasMandatory = properties.stream().anyMatch(property -> !property.isOptional());
        var hasNumber = properties.stream().anyMatch(property -> property.descriptor().isNumber());

        if (hasEnum || hasInterface || hasMandatory || hasNumber) {
            builder.beginControlFlow("error: try");
        }
        else {
            builder.beginControlFlow("error:");
        }

        final var builderName = target.interfaceType().simpleName() + "Builder";
        builder
            .beginControlFlow("if (token.type() != $T.OBJECT)", JsonType.class)
            .addStatement("errorMessage = \"expected object\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement("final var builder = new $N()", builderName)
            .beginControlFlow("for (n = token.nChildren(); n != 0; --n)")
            .beginControlFlow("switch (buffer.next().readString(reader))");

        for (final var property : properties) {
            try {
                builder.beginControlFlow("case $S:", property.nameFor(DtoEncoding.JSON));
                final var name = property.name();
                readValue(target, property.type(), x -> "builder." + name + "(" + x + ")", builder);
                builder.endControlFlow("break");
            }
            catch (final IllegalStateException exception) {
                throw new DtoException(property.parentElement(), exception.getMessage());
            }
        }

        builder
            .beginControlFlow("default:")
            .addStatement("buffer.skipValue()")
            .endControlFlow("break");

        builder
            .endControlFlow()
            .endControlFlow()
            .addStatement("return builder.build()")
            .endControlFlow();

        if (hasInterface) {
            builder
                .beginControlFlow("catch (final $T exception)", DecoderReadUnexpectedToken.class)
                .addStatement("errorMessage = \"failed to read child object\"")
                .addStatement("errorCause = exception")
                .endControlFlow();
        }
        if (hasNumber) {
            builder
                .beginControlFlow("catch (final $T exception)", NumberFormatException.class)
                .addStatement("errorMessage = \"invalid number\"")
                .addStatement("errorCause = exception")
                .endControlFlow();
        }
        if (hasMandatory) {
            builder
                .beginControlFlow("catch (final $T exception)", NullPointerException.class)
                .addStatement("errorMessage = \"required field '\" + exception.getMessage() + " +
                    "\"' not specified\"")
                .addStatement("errorCause = exception")
                .endControlFlow();
        }
        if (hasEnum) {
            builder
                .beginControlFlow("catch (final $T exception)", IllegalArgumentException.class)
                .addStatement("errorMessage = \"DTO invariant not satisfied\"")
                .addStatement("errorCause = exception")
                .endControlFlow();
        }

        builder
            .addStatement("final var atEnd = n == 0")
            .addStatement("throw new $1T($2T.JSON, reader, " +
                    "atEnd ? \"{\" : token.readStringRaw(reader), atEnd ? 0 " +
                    ": token.begin(), errorMessage, errorCause)",
                DecoderReadUnexpectedToken.class, Encoding.class);

        implementation.addMethod(builder.build());
    }

    private void readValue(
        final DtoTarget target,
        final DtoType type,
        final Expander assignment,
        final MethodSpec.Builder builder
    ) throws DtoException {
        final var descriptor = type.descriptor();
        switch (descriptor) {
        case ARRAY:
        case LIST:
            readArray(target, (DtoSequence) type, assignment, builder);
            break;

        case BIG_DECIMAL:
            readNumber("BigDecimal", assignment, builder);
            break;

        case BIG_INTEGER:
            readNumber("BigInteger", assignment, builder);
            break;

        case BOOLEAN_BOXED:
        case BOOLEAN_UNBOXED:
            readBoolean(assignment, builder);
            break;

        case BYTE_BOXED:
        case BYTE_UNBOXED:
            readNumber("Byte", assignment, builder);
            break;

        case CHARACTER_BOXED:
        case CHARACTER_UNBOXED:
            throw characterTypesNotSupportedException();

        case CUSTOM:
            readCustom(target, (DtoCustom) type, assignment, builder);
            break;

        case DOUBLE_BOXED:
        case DOUBLE_UNBOXED:
            readNumber("Double", assignment, builder);
            break;

        case DURATION:
            readTemporal(Duration.class, true, assignment, builder);
            break;

        case ENUM:
            readEnum(type, assignment, builder);
            break;

        case FLOAT_BOXED:
        case FLOAT_UNBOXED:
            readNumber("Float", assignment, builder);
            break;

        case INTEGER_BOXED:
        case INTEGER_UNBOXED:
            readNumber("Integer", assignment, builder);
            break;

        case INSTANT:
            readTemporal(Instant.class, descriptor.isNumber(), assignment, builder);
            break;

        case INTERFACE:
            readInterface((DtoInterface) type, assignment, builder);
            break;

        case LONG_BOXED:
        case LONG_UNBOXED:
            readNumber("Long", assignment, builder);
            break;

        case MAP:
            readMap(target, (DtoMap) type, assignment, builder);
            break;

        case MONTH_DAY:
            readTemporal(MonthDay.class, descriptor.isNumber(), assignment, builder);
            break;

        case OFFSET_DATE_TIME:
            readTemporal(OffsetDateTime.class, descriptor.isNumber(), assignment, builder);
            break;

        case OFFSET_TIME:
            readTemporal(OffsetTime.class, descriptor.isNumber(), assignment, builder);
            break;

        case PERIOD:
            readTemporal(Period.class, descriptor.isNumber(), assignment, builder);
            break;

        case SHORT_BOXED:
        case SHORT_UNBOXED:
            readNumber("Short", assignment, builder);
            break;

        case STRING:
            readString(assignment, builder);
            break;

        case YEAR:
            readTemporal(Year.class, descriptor.isNumber(), assignment, builder);
            break;

        case YEAR_MONTH:
            readTemporal(YearMonth.class, descriptor.isNumber(), assignment, builder);
            break;

        case ZONED_DATE_TIME:
            readTemporal(ZonedDateTime.class, descriptor.isNumber(), assignment, builder);
            break;

        case ZONE_ID:
            readTemporal(ZoneId.class, descriptor.isNumber(), assignment, builder);
            break;

        case ZONE_OFFSET:
            readTemporal(ZoneOffset.class, descriptor.isNumber(), assignment, builder);
            break;

        default:
            throw new IllegalStateException("Unexpected type: " + type);
        }
    }

    private void readArray(
        final DtoTarget target,
        final DtoSequence type,
        final Expander assignment,
        final MethodSpec.Builder builder
    ) throws DtoException {
        final var element = type.element();
        final var elementTypeName = element.inputTypeName();

        builder
            .addStatement("token = buffer.next()")
            .addStatement("type = token.type()");
        if (level == 0) {
            builder
                .beginControlFlow("if (type == $T.NULL)", JsonType.class)
                .addStatement("continue")
                .endControlFlow();
        }
        builder
            .beginControlFlow("if (type != $T.ARRAY)", JsonType.class)
            .addStatement("errorMessage = \"expected array\"")
            .addStatement("break error")
            .endControlFlow();

        final Expander assignment0;
        if (type.descriptor() == DtoDescriptor.ARRAY) {
            builder
                .addStatement("final var items$L = new $T[token.nChildren()]", level, elementTypeName)
                .beginControlFlow("for (var i$1L = 0; i$1L < items$1L.length; ++i$1L)", level);

            final var leader = "items" + level + "[i" + level + "] = ";
            assignment0 = x -> leader + x;
        }
        else {
            builder
                .addStatement("var n$L = token.nChildren()", level)
                .addStatement("final var items$1L = new $2T<$3T>(n$1L)", level, ArrayList.class, elementTypeName)
                .beginControlFlow("while (n$1L-- != 0)", level);

            final var leader = "items" + level + ".add(";
            assignment0 = x -> leader + x + ")";
        }

        level += 1;
        readValue(target, element, assignment0, builder);
        level -= 1;

        builder
            .endControlFlow()
            .addStatement(assignment.expand("items$L"), level);
    }

    private void readBoolean(final Expander assignment, final MethodSpec.Builder builder) {
        builder
            .addStatement("boolean value$L", level)
            .beginControlFlow("switch (buffer.next().type())")
            .addStatement("case TRUE: value$L = true; break", level)
            .addStatement("case FALSE: value$L = false; break", level);
        if (level == 0) {
            builder.addStatement("case NULL: continue");
        }
        builder
            .beginControlFlow("default:")
            .addStatement("errorMessage = \"expected 'true' or 'false'\"")
            .addStatement("break error")
            .endControlFlow()
            .endControlFlow()
            .addStatement(assignment.expand("value$L"), level);
    }

    private void readCustom(
        final DtoTarget target,
        final DtoCustom type,
        final Expander assignment,
        final MethodSpec.Builder builder
    )
        throws DtoException
    {
        final var returnType = target.interfaceType().type().toString();
        final var parameter = JsonTokenBuffer.class.getCanonicalName();
        if (!type.containsPublicStaticMethod(returnType, "readJson", parameter)) {
            throw new DtoException(type.typeElement(), "No public static " +
                "readJson(JsonTokenBuffer) method available; required for " +
                "this class/interface to be useful as a custom JSON DTO");
        }

        if (level == 0) {
            builder
                .beginControlFlow("if (buffer.peek().type() == $T.NULL)", JsonType.class)
                .addStatement("buffer.skipElement()")
                .addStatement("continue")
                .endControlFlow();
        }

        builder.addStatement(assignment.expand("$T.readJson(buffer)"), type.inputTypeName());
    }

    private void readEnum(final DtoType type, final Expander assignment, final MethodSpec.Builder builder) {
        builder
            .addStatement("token = buffer.next()")
            .addStatement("type = token.type()");
        if (level == 0) {
            builder
                .beginControlFlow("if (type == $T.NULL)", JsonType.class)
                .addStatement("continue")
                .endControlFlow();
        }
        builder
            .beginControlFlow("if (type != $T.STRING)", JsonType.class)
            .addStatement("errorMessage = \"expected number\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement(assignment.expand("$T.valueOf(token.readString(reader))"), type.inputTypeName());
    }

    private void readInterface(final DtoInterface type, final Expander assignment, final MethodSpec.Builder builder) {
        if (!type.isReadable(DtoEncoding.JSON)) {
            throw new IllegalStateException(type.simpleName() + " is not annotated with " +
                "@DtoReadableAs, or lacks DataEncoding.JSON as annotation argument");
        }

        if (level == 0) {
            builder
                .beginControlFlow("if (buffer.peek().type() == $T.NULL)", JsonType.class)
                .addStatement("buffer.skipElement()")
                .addStatement("continue")
                .endControlFlow();
        }

        builder.addStatement(assignment.expand("$T.readJson(buffer)"), type.inputTypeName());
    }

    private void readMap(
        final DtoTarget target,
        final DtoMap type,
        final Expander assignment,
        final MethodSpec.Builder builder
    )
        throws DtoException
    {
        final var key = type.key();
        final var value = type.value();

        builder
            .addStatement("token = buffer.next()")
            .addStatement("type = token.type()");
        if (level == 0) {
            builder
                .beginControlFlow("if (type == $T.NULL)", JsonType.class)
                .addStatement("continue")
                .endControlFlow();
        }
        builder
            .beginControlFlow("if (type != $T.OBJECT)", JsonType.class)
            .addStatement("errorMessage = \"expected object\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement("var n$L = token.nChildren()", level)
            .addStatement("final var entries$1L = new $2T<$3T, $4T>(n$1L)",
                level, HashMap.class, key.inputTypeName(), value.inputTypeName())
            .beginControlFlow("while (n$L-- != 0)", level)
            .addStatement("final var key$L = buffer.next().readString(reader)", level);

        final var leader = "final var value" + level + " = ";
        level += 1;
        readValue(target, value, x -> leader + x, builder);
        level -= 1;

        if (key.descriptor() == DtoDescriptor.ENUM) {
            builder.addStatement("entries$1L.put($2T.valueOf(key$1L), value$1L)", level, key.inputTypeName());
        }
        else {
            builder.addStatement("entries$1L.put(key$1L, value$1L)", level);
        }

        builder
            .endControlFlow()
            .addStatement(assignment.expand("entries$L"), level);
    }

    private void readNumber(final String type, final Expander assignment, final MethodSpec.Builder builder) {
        builder
            .addStatement("token = buffer.next()")
            .addStatement("type = token.type()");
        if (level == 0) {
            builder
                .beginControlFlow("if (type == $T.NULL)", JsonType.class)
                .addStatement("continue")
                .endControlFlow();
        }
        builder
            .beginControlFlow("if (type != $T.NUMBER)", JsonType.class)
            .addStatement("errorMessage = \"expected number\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement(assignment.expand("token.read" + type + "(reader)"));
    }

    private void readString(final Expander assignment, final MethodSpec.Builder builder) {
        builder
            .addStatement("token = buffer.next()")
            .addStatement("type = token.type()");
        if (level == 0) {
            builder
                .beginControlFlow("if (type == $T.NULL)", JsonType.class)
                .addStatement("continue")
                .endControlFlow();
        }
        builder
            .beginControlFlow("if (type != $T.STRING)", JsonType.class)
            .addStatement("errorMessage = \"expected string\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement(assignment.expand("token.readString(reader)"));
    }

    public void readTemporal(
        final Class<?> class_,
        final boolean asNumber, final Expander assignment,
        final MethodSpec.Builder builder
    ) {
        builder.addStatement("token = buffer.next()");
        if (asNumber) {
            builder
                .addStatement("$T value$L", class_, level)
                .beginControlFlow("switch (token.type())")
                .addStatement("case NUMBER: value$L = token.read$TNumber(reader); break", level, class_)
                .addStatement("case STRING: value$L = token.read$T(reader); break", level, class_);
            if (level == 0) {
                builder.addStatement("case NULL: continue");
            }
            builder
                .addStatement("default: errorMessage = \"expected number or string\"; break error")
                .endControlFlow()
                .addStatement(assignment.expand("value$L"), level);
        }
        else {
            builder.addStatement("type = token.type()");
            if (level == 0) {
                builder
                    .beginControlFlow("if (type == $T.NULL)", JsonType.class)
                    .addStatement("continue")
                    .endControlFlow();
            }
            builder
                .beginControlFlow("if (type != $T.STRING)", JsonType.class)
                .addStatement("errorMessage = \"expected string\"")
                .addStatement("break error")
                .endControlFlow()
                .addStatement(assignment.expand("token.read$T(reader)"), class_);
        }
    }

    private void implementWriteMethodFor(final DtoTarget target, final TypeSpec.Builder implementation)
        throws DtoException
    {
        final var builder = MethodSpec.methodBuilder("writeJson")
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.get(Encoding.class))
            .addParameter(ParameterSpec.builder(TypeName.get(BinaryWriter.class), "writer")
                .addModifiers(Modifier.FINAL)
                .build());

        writeCache.clear();
        writeCache.append('{');

        final var properties = target.properties();
        final var p1 = properties.size();
        var checkBeforeWritingComma = false;
        if (p1 > 0) {
            final var property0 = properties.get(0);
            if (property0.isOptional() || property0.descriptor().isCollection()) {
                checkBeforeWritingComma = true;
                builder.addStatement("var addComma = false");
            }
        }
        var stopCheckingBeforeWritingComma = false;
        for (var p0 = 0; p0 < p1; ++p0) {
            final var property = properties.get(p0);
            final var descriptor = property.descriptor();
            final var isCollection = descriptor.isCollection();
            final var isOptional = property.isOptional();
            final var name = property.name();
            try {
                if (isOptional) {
                    writeCache.addWriteIfNotEmpty(builder);
                    builder.beginControlFlow("if ($N != null)", name);
                }
                else if (isCollection) {
                    writeCache.addWriteIfNotEmpty(builder);
                    if (descriptor == DtoDescriptor.ARRAY) {
                        builder.beginControlFlow("if ($N.length != 0)", name);
                    }
                    else {
                        builder.beginControlFlow("if (!$N.isEmpty())", name);
                    }
                }
                else {
                    stopCheckingBeforeWritingComma = true;
                }

                if (p0 != 0) {
                    if (checkBeforeWritingComma) {
                        writeCache.addWriteIfNotEmpty(builder);
                        builder.beginControlFlow("if (addComma)");
                        writeCache.append(',').addWrite(builder);
                        builder.endControlFlow();
                    }
                    else {
                        writeCache.append(',');
                    }
                }

                if (stopCheckingBeforeWritingComma) {
                    checkBeforeWritingComma = false;
                }

                writeCache
                    .append('"')
                    .append(property.nameFor(DtoEncoding.JSON))
                    .append("\":");

                writeValue(property.type(), name, builder);

                if (isOptional || isCollection) {
                    writeCache.addWriteIfNotEmpty(builder);
                    if (checkBeforeWritingComma) {
                        builder.addStatement("addComma = true");
                    }
                    builder.endControlFlow();
                }
            }
            catch (final IllegalStateException exception) {
                throw new DtoException(property.parentElement(), exception.getMessage());
            }
        }

        writeCache.append('}').addWriteIfNotEmpty(builder);

        builder.addStatement("return $T.JSON", Encoding.class);

        implementation.addMethod(builder.build());
    }

    private void writeValue(final DtoType type, final String name, final MethodSpec.Builder builder)
        throws DtoException
    {
        final var descriptor = type.descriptor();
        switch (descriptor) {
        case ARRAY:
        case LIST:
            writeArray(type, name, builder);
            break;

        case BOOLEAN_BOXED:
        case BOOLEAN_UNBOXED:
        case BIG_DECIMAL:
        case BIG_INTEGER:
        case BYTE_BOXED:
        case BYTE_UNBOXED:
        case DOUBLE_BOXED:
        case DOUBLE_UNBOXED:
        case FLOAT_BOXED:
        case FLOAT_UNBOXED:
        case INTEGER_BOXED:
        case INTEGER_UNBOXED:
        case LONG_BOXED:
        case LONG_UNBOXED:
        case SHORT_BOXED:
        case SHORT_UNBOXED:
            writeOther(name, builder);
            break;

        case CHARACTER_BOXED:
        case CHARACTER_UNBOXED:
            throw characterTypesNotSupportedException();

        case CUSTOM:
            writeCustom((DtoCustom) type, name, builder);
            break;

        case DURATION:
        case INSTANT:
        case MONTH_DAY:
        case OFFSET_DATE_TIME:
        case OFFSET_TIME:
        case PERIOD:
        case STRING:
        case YEAR:
        case YEAR_MONTH:
        case ZONED_DATE_TIME:
        case ZONE_ID:
        case ZONE_OFFSET:
            writeString(name, builder);
            break;

        case ENUM:
            writeEnum(name, builder);
            break;

        case INTERFACE:
            writeInterface((DtoInterface) type, name, builder);
            break;

        case MAP:
            writeMap(type, name, builder);
            break;

        default:
            throw new IllegalStateException("Unexpected type: " + type);
        }
    }

    private void writeArray(final DtoType type, final String name, final MethodSpec.Builder builder)
        throws DtoException
    {
        if (level > 0) {
            builder.beginControlFlow("");
        }

        writeCache.append('[').addWrite(builder);

        builder
            .addStatement("var i$L = 0", level)
            .beginControlFlow("for (final var item$L : $N)", level, name)
            .beginControlFlow("if (i$L++ != 0)", level)
            .addStatement("writer.write((byte) ',')")
            .endControlFlow();

        final var itemName = "item" + level;
        level += 1;
        writeValue(((DtoSequence) type).element(), itemName, builder);
        level -= 1;

        writeCache.addWriteIfNotEmpty(builder);
        builder.endControlFlow();

        writeCache.append(']');

        if (level > 0) {
            builder.endControlFlow();
        }
    }

    private void writeCustom(
        final DtoCustom type,
        final String name,
        final MethodSpec.Builder builder
    )
        throws DtoException
    {
        final var returnType = Encoding.class.getCanonicalName();
        final var parameter = BinaryWriter.class.getCanonicalName();
        if (!type.containsPublicMethod(returnType, "writeJson", parameter)) {
            throw new DtoException(type.typeElement(), "No public " +
                "writeJson(BinaryWriter) method available; required for " +
                "this class/interface to be useful as a custom JSON DTO");
        }

        writeCache.addWriteIfNotEmpty(builder);
        builder.addStatement("$N.writeJson(writer)", name);
    }

    private void writeEnum(final String name, final MethodSpec.Builder builder) {
        writeCache.append('"').addWrite(builder);
        builder.addStatement("$T.write($N.toString(), writer)", JsonWrite.class, name);
        writeCache.append('"');
    }

    private void writeInterface(final DtoInterface type, final String name, final MethodSpec.Builder builder) {
        if (!type.isWritable(DtoEncoding.JSON)) {
            throw new IllegalStateException(type.simpleName() + " is not " +
                "annotated with @DtoWritableAs, or lacks DataEncoding.JSON as " +
                "annotation argument");
        }
        writeCache.addWriteIfNotEmpty(builder);
        builder.addStatement("$N.writeJson(writer)", name);
    }

    private void writeMap(final DtoType type, final String name, final MethodSpec.Builder builder)
        throws DtoException
    {
        if (level > 0) {
            builder.beginControlFlow("");
        }

        writeCache.append('{').addWrite(builder);

        final var map = (DtoMap) type;
        builder
            .addStatement("final var entrySet$L = $N.entrySet()", level, name)
            .addStatement("var i$L = 0", level)
            .beginControlFlow("for (final var entry$1L : entrySet$1L)", level)
            .beginControlFlow("if (i$L++ != 0)", level)
            .addStatement("writer.write((byte) ',')")
            .endControlFlow();

        writeValue(map.key(), "entry" + level + ".getKey()", builder);

        writeCache.append(':');

        final var valueName = "entry" + level + ".getValue()";
        level += 1;
        writeValue(map.value(), valueName, builder);
        level -= 1;

        writeCache.addWriteIfNotEmpty(builder);
        builder.endControlFlow();

        writeCache.append('}');

        if (level > 0) {
            builder.endControlFlow();
        }
    }

    private void writeOther(final String name, final MethodSpec.Builder builder) {
        writeCache.addWriteIfNotEmpty(builder);
        builder.addStatement("$T.write($N, writer)", JsonWrite.class, name);
    }

    private void writeString(final String name, final MethodSpec.Builder builder) {
        writeCache.append('"').addWrite(builder);
        builder.addStatement("$T.write($N, writer)", JsonWrite.class, name);
        writeCache.append('"');
    }

    private static IllegalStateException characterTypesNotSupportedException() {
        return new IllegalStateException("The char and Character types " +
            "cannot be represented as JSON; either change the type or " +
            "remove DataEncoding.JSON from the array of encodings " +
            "provided to the @DtoReadableAs/@DtoWritableAs annotation(s)");
    }
}
