package se.arkalix.dto.json.value;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.binary.BinaryWriter;
import se.arkalix.dto.json.JsonType;
import se.arkalix.internal.dto.json.JsonTokenBuffer;
import se.arkalix.internal.dto.json.JsonTokenizer;
import se.arkalix.internal.dto.json.JsonWriter;
import se.arkalix.util.annotation.Internal;

import java.time.*;

@SuppressWarnings("unused")
public class JsonString implements JsonValue {
    private final String string;

    public JsonString(final String string) {
        this.string = string;
    }

    public JsonString(final Duration string) {
        this.string = string.toString();
    }

    public JsonString(final Instant string) {
        this.string = string.toString();
    }

    public JsonString(final MonthDay string) {
        this.string = string.toString();
    }

    public JsonString(final OffsetDateTime string) {
        this.string = string.toString();
    }

    public JsonString(final OffsetTime string) {
        this.string = string.toString();
    }

    public JsonString(final Period string) {
        this.string = string.toString();
    }

    @Override
    public JsonType type() {
        return JsonType.STRING;
    }

    public Duration toDuration() {
        return Duration.parse(string);
    }

    public Instant toInstant() {
        return Instant.parse(string);
    }

    public MonthDay toMonthDay() {
        return MonthDay.parse(string);
    }

    public OffsetDateTime toOffsetDateTime() {
        return OffsetDateTime.parse(string);
    }

    public OffsetTime toOffsetTime() {
        return OffsetTime.parse(string);
    }

    public Period toPeriod() {
        return Period.parse(string);
    }

    @Override
    public String toString() {
        return string;
    }

    public static JsonString readJson(final BinaryReader source) throws DtoReadException {
        return readJson(JsonTokenizer.tokenize(source));
    }

    @Internal
    public static JsonString readJson(final JsonTokenBuffer buffer) throws DtoReadException {
        final var source = buffer.source();
        var token = buffer.next();
        if (token.type() != JsonType.STRING) {
            throw new DtoReadException(DtoEncoding.JSON, "Expected string",
                token.readStringRaw(source), token.begin());
        }
        return new JsonString(token.readString(source));
    }

    @Override
    public void writeJson(final BinaryWriter writer) {
        writer.write((byte) '"');
        JsonWriter.write(string, writer);
        writer.write((byte) '"');
    }
}
