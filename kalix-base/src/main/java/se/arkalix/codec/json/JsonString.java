package se.arkalix.codec.json;

import se.arkalix.codec.CodecType;
import se.arkalix.codec.DecoderReadUnexpectedToken;
import se.arkalix.codec.binary.BinaryReader;
import se.arkalix.codec.binary.BinaryWriter;
import se.arkalix.codec.json._internal.JsonTokenBuffer;
import se.arkalix.codec.json._internal.JsonTokenizer;
import se.arkalix.codec.json._internal.JsonPrimitives;
import se.arkalix.util.annotation.Internal;

import java.time.*;
import java.util.Objects;
import java.util.Optional;

/**
 * JSON string.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
@SuppressWarnings("unused")
public class JsonString implements JsonValue {
    private final String string;

    /**
     * Creates new JSON string from given Java {@link String}.
     *
     * @param string String.
     */
    public JsonString(final String string) {
        this.string = Objects.requireNonNull(string, "string");
    }

    /**
     * Creates new JSON string from given {@link Duration}.
     *
     * @param duration Duration.
     */
    public JsonString(final Duration duration) {
        this.string = Objects.requireNonNull(duration, "string").toString();
    }

    /**
     * Creates new JSON string from given {@link Instant}.
     *
     * @param instant Instant.
     */
    public JsonString(final Instant instant) {
        this.string = Objects.requireNonNull(instant, "string").toString();
    }

    /**
     * Creates new JSON string from given {@link MonthDay}.
     *
     * @param monthDay Month and day.
     */
    public JsonString(final MonthDay monthDay) {
        this.string = Objects.requireNonNull(monthDay, "string").toString();
    }

    /**
     * Creates new JSON string from given {@link OffsetDateTime}.
     *
     * @param offsetDateTime Date and time with time zone offset.
     */
    public JsonString(final OffsetDateTime offsetDateTime) {
        this.string = Objects.requireNonNull(offsetDateTime, "string").toString();
    }

    /**
     * Creates new JSON string from given {@link OffsetTime}.
     *
     * @param offsetTime Time with time zone offset.
     */
    public JsonString(final OffsetTime offsetTime) {
        this.string = Objects.requireNonNull(offsetTime, "string").toString();
    }

    /**
     * Creates new JSON string from given {@link Period}.
     *
     * @param period Period.
     */
    public JsonString(final Period period) {
        this.string = Objects.requireNonNull(period, "string").toString();
    }

    @Override
    public JsonType type() {
        return JsonType.STRING;
    }

    /**
     * @return This JSON string converted to a {@link Duration}.
     * @throws java.time.format.DateTimeParseException If this string does not
     *                                                 contain a valid ISO8601
     *                                                 duration string.
     */
    public Duration toDuration() {
        return Duration.parse(string);
    }

    /**
     * @return This JSON string converted to a {@link Instant}.
     * @throws java.time.format.DateTimeParseException If this string does not
     *                                                 contain a valid ISO8601
     *                                                 date and time string.
     */
    public Instant toInstant() {
        return Instant.parse(string);
    }

    /**
     * @return This JSON string converted to a {@link MonthDay}.
     * @throws java.time.format.DateTimeParseException If this string does not
     *                                                 contain a valid ISO8601
     *                                                 month and day string.
     */
    public MonthDay toMonthDay() {
        return MonthDay.parse(string);
    }

    /**
     * @return This JSON string converted to a {@link OffsetDateTime}.
     * @throws java.time.format.DateTimeParseException If this string does not
     *                                                 contain a valid ISO8601
     *                                                 date and time string.
     */
    public OffsetDateTime toOffsetDateTime() {
        return OffsetDateTime.parse(string);
    }

    /**
     * @return This JSON string converted to a {@link OffsetTime}.
     * @throws java.time.format.DateTimeParseException If this string does not
     *                                                 contain a valid ISO8601
     *                                                 time string.
     */
    public OffsetTime toOffsetTime() {
        return OffsetTime.parse(string);
    }

    /**
     * @return This JSON string converted to a {@link Period}.
     * @throws java.time.format.DateTimeParseException If this string does not
     *                                                 contain a valid ISO8601
     *                                                 period string.
     */
    public Period toPeriod() {
        return Period.parse(string);
    }

    /**
     * Reads JSON string from given {@code reader}.
     *
     * @param reader Source containing JSON string at the current read offset,
     *               ignoring any whitespace.
     * @return Decoded JSON string.
     * @throws DecoderReadUnexpectedToken If the reader does not contain a
     *                                    valid JSON string at the current read
     *                                    offset.
     */
    public static JsonString readJson(final BinaryReader reader) {
        return readJson(JsonTokenizer.tokenize(reader));
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
    @Internal
    public static JsonString readJson(final JsonTokenBuffer buffer) {
        final var reader = buffer.reader();
        var token = buffer.next();
        if (token.type() != JsonType.STRING) {
            throw new DecoderReadUnexpectedToken(
                CodecType.JSON,
                reader,
                JsonPrimitives.readStringRaw(token, reader),
                token.begin(),
                "expected string");
        }
        return new JsonString(JsonPrimitives.readString(token, reader));
    }

    @Override
    public CodecType writeJson(final BinaryWriter writer) {
        writer.write((byte) '"');
        JsonPrimitives.write(string, writer);
        writer.write((byte) '"');
        return CodecType.JSON;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final JsonString that = (JsonString) other;
        return string.equals(that.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string);
    }

    @Override
    public String toString() {
        return string;
    }

    @Override
    public Optional<String> tryToString() {
        return Optional.of(string);
    }
}
