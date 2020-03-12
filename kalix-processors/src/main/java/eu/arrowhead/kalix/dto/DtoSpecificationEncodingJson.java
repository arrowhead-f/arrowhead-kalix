package eu.arrowhead.kalix.dto;

import com.squareup.javapoet.*;
import eu.arrowhead.kalix.dto.binary.BinaryReader;
import eu.arrowhead.kalix.dto.binary.BinaryWriter;
import eu.arrowhead.kalix.dto.json.*;
import eu.arrowhead.kalix.dto.types.*;
import eu.arrowhead.kalix.dto.util.BinaryWriterWriteCache;
import eu.arrowhead.kalix.dto.util.Expander;

import javax.lang.model.element.Modifier;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DtoSpecificationEncodingJson implements DtoSpecificationEncoding {
    private final BinaryWriterWriteCache writeCache = new BinaryWriterWriteCache("target");

    private int level = 0;

    @Override
    public DataEncoding encoding() {
        return DataEncoding.JSON;
    }

    @Override
    public void implementFor(final DtoTarget target, final TypeSpec.Builder implementation) throws DtoException {
        if (target.interfaceType().isReadable(DataEncoding.JSON)) {
            implementation.addSuperinterface(JsonReadable.class);
            implementReadMethodsFor(target, implementation);
        }
        if (target.interfaceType().isWritable(DataEncoding.JSON)) {
            implementation.addSuperinterface(JsonWritable.class);
            implementWriteMethodFor(target, implementation);
        }
    }

    private void implementReadMethodsFor(final DtoTarget target, final TypeSpec.Builder implementation) throws DtoException {
        final var dataTypeName = target.dataTypeName();

        implementation.addMethod(MethodSpec.methodBuilder("readJson")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(dataTypeName)
            .addParameter(BinaryReader.class, "source", Modifier.FINAL)
            .addException(ReadException.class)
            .addStatement("return readJson($T.tokenize(source))", JsonReader.class)
            .build());

        final var builder = MethodSpec.methodBuilder("readJson")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(dataTypeName)
            .addParameter(JsonTokenReader.class, "reader", Modifier.FINAL)
            .addException(ReadException.class)
            .addStatement("final var source = reader.source()")
            .addStatement("var token = reader.next()")
            .addStatement("var error = \"\"");

        var hasEnum = target.properties().stream().anyMatch(property -> property.descriptor() == DtoDescriptor.ENUM);
        var hasMandatory = target.properties().stream().anyMatch(property -> !property.isOptional());
        var hasNumber = target.properties().stream().anyMatch(property -> property.descriptor().isNumber());

        if (hasEnum || hasMandatory || hasNumber) {
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
            try {
                builder.beginControlFlow("case $S:", property.nameFor(DataEncoding.JSON));
                final var name = property.name();
                readValue(property.type(), x -> "builder." + name + "(" + x + ")", builder);
                builder.endControlFlow("break");
            }
            catch (final IllegalStateException exception) {
                throw new DtoException(property.parentElement(), exception.getMessage());
            }
        }

        builder
            .endControlFlow()
            .endControlFlow()
            .addStatement("return builder.build()")
            .endControlFlow();

        if (hasNumber) {
            builder
                .beginControlFlow("catch (final $T ignored)", NumberFormatException.class)
                .addStatement("error = \"Invalid number\"")
                .endControlFlow();
        }
        if (hasMandatory) {
            builder
                .beginControlFlow("catch (final $T exception)", NullPointerException.class)
                .addStatement("error = \"Mandatory field `\" + exception.getMessage() + \"` missing in object\"")
                .endControlFlow();
        }
        if (hasEnum) {
            builder
                .beginControlFlow("catch (final $T exception)", IllegalArgumentException.class)
                .addStatement("error = exception.getMessage()")
                .endControlFlow();
        }

        builder.addCode("throw new $1T($2T.JSON, error, token.readString(source), token.begin());\n",
            ReadException.class, DataEncoding.class);

        implementation.addMethod(builder.build());
    }

    private void readValue(final DtoType type, final Expander assignment, final MethodSpec.Builder builder) {
        final var descriptor = type.descriptor();
        switch (descriptor) {
        case ARRAY:
        case LIST:
            readArray((DtoSequence) type, assignment, builder);
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

        case LOCAL_DATE:
            readTemporal(LocalDate.class, descriptor.isNumber(), assignment, builder);
            break;

        case LOCAL_DATE_TIME:
            readTemporal(LocalDateTime.class, descriptor.isNumber(), assignment, builder);
            break;

        case LOCAL_TIME:
            readTemporal(LocalTime.class, descriptor.isNumber(), assignment, builder);
            break;

        case LONG_BOXED:
        case LONG_UNBOXED:
            readNumber("Long", assignment, builder);
            break;

        case MAP:
            readMap((DtoMap) type, assignment, builder);
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

    private void readArray(final DtoSequence type, final Expander assignment, final MethodSpec.Builder builder) {
        final var element = type.element();
        final var elementTypeName = element.inputTypeName();

        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.ARRAY)", JsonType.class)
            .addStatement("error = \"Expected array\"")
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
        readValue(element, assignment0, builder);
        level -= 1;

        builder
            .endControlFlow()
            .addStatement(assignment.expand("items$L"), level);
    }

    private void readBoolean(final Expander assignment, final MethodSpec.Builder builder) {
        builder
            .addStatement("boolean value$L", level)
            .beginControlFlow("switch (reader.next().type())")
            .addStatement("case TRUE: value$L = true; break", level)
            .addStatement("case FALSE: value$L = false; break", level)
            .beginControlFlow("default:")
            .addStatement("error = \"Expected true or false\"")
            .addStatement("break error")
            .endControlFlow()
            .endControlFlow()
            .addStatement(assignment.expand("value$L"), level);
    }

    private void readEnum(final DtoType type, final Expander assignment, final MethodSpec.Builder builder) {
        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.STRING)", JsonType.class)
            .addStatement("error = \"Expected number\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement(assignment.expand("$T.valueOf(token.readString(source))"), type.inputTypeName());
    }

    private void readInterface(final DtoInterface type, final Expander assignment, final MethodSpec.Builder builder) {
        if (!type.isReadable(DataEncoding.JSON)) {
            throw new IllegalStateException(type.simpleName() + " is not annotated with " +
                "@Readable, or lacks DataEncoding.JSON as annotation argument");
        }
        builder.addStatement(assignment.expand("$T.readJson(reader)"), type.inputTypeName());
    }

    private void readMap(final DtoMap type, final Expander assignment, final MethodSpec.Builder builder) {
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
                level, HashMap.class, key.inputTypeName(), value.inputTypeName())
            .beginControlFlow("while (n$L-- != 0)", level)
            .addStatement("final var key$L = reader.next().readString(source)", level);

        final var leader = "final var value" + level + " = ";
        level += 1;
        readValue(value, x -> leader + x, builder);
        level -= 1;

        builder
            .addStatement("entries$1L.put(key$1L, value$1L)", level)
            .endControlFlow()
            .addStatement(assignment.expand("entries$L"), level);
    }

    private void readNumber(final String type, final Expander assignment, final MethodSpec.Builder builder) {
        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.NUMBER)", JsonType.class)
            .addStatement("error = \"Expected number\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement(assignment.expand("token.read" + type + "(source)"));
    }

    private void readString(final Expander assignment, final MethodSpec.Builder builder) {
        builder
            .addStatement("token = reader.next()")
            .beginControlFlow("if (token.type() != $T.STRING)", JsonType.class)
            .addStatement("error = \"Expected string\"")
            .addStatement("break error")
            .endControlFlow()
            .addStatement(assignment.expand("token.readString(source)"));
    }

    public void readTemporal(
        final Class<?> class_,
        final boolean asNumber, final Expander assignment,
        final MethodSpec.Builder builder)
    {
        builder.addStatement("token = reader.next()");
        if (asNumber) {
            builder
                .addStatement("$T value$L", class_, level)
                .beginControlFlow("switch (token.type())")
                .addStatement("case NUMBER: value$L = token.read$TNumber(source); break", level, class_)
                .addStatement("case STRING: value$L = token.read$T(source); break", level, class_)
                .addStatement("default: error = \"Expected number or string\"; break error")
                .endControlFlow()
                .addStatement(assignment.expand("value$L"), level);
        }
        else {
            builder
                .beginControlFlow("if (token.type() != $T.STRING)", JsonType.class)
                .addStatement("error = \"Expected string\"")
                .addStatement("break error")
                .endControlFlow()
                .addStatement(assignment.expand("token.read$T(source)"), class_);
        }
    }

    private void implementWriteMethodFor(final DtoTarget target, final TypeSpec.Builder implementation)
        throws DtoException
    {
        final var builder = MethodSpec.methodBuilder("writeJson")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addException(WriteException.class)
            .addParameter(ParameterSpec.builder(TypeName.get(BinaryWriter.class), "target")
                .addModifiers(Modifier.FINAL)
                .build());

        writeCache.clear();
        writeCache.append('{');

        final var properties = target.properties();
        final var p1 = properties.size();
        for (var p0 = 0; p0 < p1; ++p0) {
            final var property = properties.get(p0);
            final var isOptional = property.isOptional();
            try {
                if (p0 != 0) {
                    if (isOptional) {
                        writeCache.addWriteIfNotEmpty(builder);
                        builder.beginControlFlow("if ($N != null)", property.name());
                    }
                    writeCache.append(',');
                }

                writeCache
                    .append('"')
                    .append(property.nameFor(DataEncoding.JSON))
                    .append("\":");

                writeValue(property.type(), property.name(), builder);

                if (isOptional) {
                    writeCache.addWriteIfNotEmpty(builder);
                    builder.endControlFlow();
                }
            }
            catch (final IllegalStateException exception) {
                throw new DtoException(property.parentElement(), exception.getMessage());
            }
        }

        writeCache.append('}').addWriteIfNotEmpty(builder);

        implementation.addMethod(builder.build());
    }

    private void writeValue(final DtoType type, final String name, final MethodSpec.Builder builder) {
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

        case DURATION:
        case INSTANT:
        case LOCAL_DATE:
        case LOCAL_DATE_TIME:
        case LOCAL_TIME:
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

    private void writeArray(final DtoType type, final String name, final MethodSpec.Builder builder) {
        writeCache.append('[').addPut(builder);

        builder
            .beginControlFlow("")
            .addStatement("final var size$L = $N.$N",
                level, name, type.descriptor() == DtoDescriptor.ARRAY
                    ? "length"
                    : "size()")
            .addStatement("var i$L = 0", level)
            .beginControlFlow("for (final var item$L : $N)", level, name)
            .beginControlFlow("if (i$L++ != 0)", level)
            .addStatement("target.write((byte) ',')")
            .endControlFlow();

        final var itemName = "item" + level;
        level += 1;
        writeValue(((DtoSequence) type).element(), itemName, builder);
        level -= 1;

        writeCache.addWriteIfNotEmpty(builder);
        builder
            .endControlFlow()
            .endControlFlow();

        writeCache.append(']');
    }

    private void writeEnum(final String name, final MethodSpec.Builder builder) {
        writeCache.append('"').addPut(builder);
        builder.addStatement("$T.write($N.toString(), target)", JsonWriter.class, name);
        writeCache.append('"');
    }

    private void writeInterface(final DtoInterface type, final String name, final MethodSpec.Builder builder) {
        if (!type.isWritable(DataEncoding.JSON)) {
            throw new IllegalStateException(type.simpleName() + " is not " +
                "annotated with @Writable, or lacks DataEncoding.JSON as " +
                "annotation argument");
        }
        writeCache.addWriteIfNotEmpty(builder);
        builder.addStatement("$N.writeJson(target)", name);
    }

    private void writeMap(final DtoType type, final String name, final MethodSpec.Builder builder) {
        writeCache.append('{').addWriteIfNotEmpty(builder);

        final var map = (DtoMap) type;
        builder
            .beginControlFlow("")
            .addStatement("final var entrySet$L = $N.entrySet()", level, name)
            .addStatement("final var size$1L = entrySet$1L.size()", level)
            .addStatement("var i$L = 0", level)
            .beginControlFlow("for (final var entry$1L : entrySet$1L)", level)
            .beginControlFlow("if (i$L++ != 0)", level)
            .addStatement("target.write((byte) ',')")
            .endControlFlow();

        writeValue(map.key(), "entry" + level + ".getKey()", builder);

        writeCache.append(':');

        final var valueName = "entry" + level + ".getValue()";
        level += 1;
        writeValue(map.value(), valueName, builder);
        level -= 1;

        writeCache.addWriteIfNotEmpty(builder);
        builder
            .endControlFlow()
            .endControlFlow();

        writeCache.append('}');
    }

    private void writeOther(final String name, final MethodSpec.Builder builder) {
        writeCache.addWriteIfNotEmpty(builder);
        builder.addStatement("$T.write($N, target)", JsonWriter.class, name);
    }

    private void writeString(final String name, final MethodSpec.Builder builder) {
        writeCache.append('"').addPut(builder);
        builder.addStatement("$T.write($N, target)", JsonWriter.class, name);
        writeCache.append('"');
    }

    private static IllegalStateException characterTypesNotSupportedException() {
        return new IllegalStateException("The char and Character types " +
            "cannot be represented as JSON; either change the type or " +
            "remove DataEncoding.JSON from the array of encodings " +
            "provided to the @Readable/@Writable annotation(s)");
    }
}
