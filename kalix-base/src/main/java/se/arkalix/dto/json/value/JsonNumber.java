package se.arkalix.dto.json.value;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.dto.binary.BinaryWriter;
import se.arkalix.dto.json.JsonType;
import se.arkalix.internal.dto.json.JsonTokenBuffer;
import se.arkalix.internal.dto.json.JsonTokenizer;
import se.arkalix.util.annotation.Internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@SuppressWarnings("unused")
public class JsonNumber implements JsonValue {
    private final String number;

    private JsonNumber(final String number) {
        this.number = number;
    }

    public JsonNumber(final BigDecimal number) {
        this.number = number.toString();
    }

    public JsonNumber(final BigInteger integer) {
        this.number = integer.toString();
    }

    public JsonNumber(final byte number) {
        this.number = Byte.toString(number);
    }

    public JsonNumber(final double number) {
        this.number = Double.toString(number);
    }

    public JsonNumber(final Duration number) {
        this.number = String.format("%d.%09d", number.toSeconds(), number.toNanosPart());
    }

    public JsonNumber(final float number) {
        this.number = Float.toString(number);
    }

    public JsonNumber(final int number) {
        this.number = Integer.toString(number);
    }

    public JsonNumber(final Instant number) {
        this.number = String.format("%d.%09d", number.getEpochSecond(), number.getNano());
    }

    public JsonNumber(final long number) {
        this.number = Long.toString(number);
    }

    public JsonNumber(final OffsetDateTime number) {
        final var instant = number.toInstant();
        this.number = String.format("%d.%09d", instant.getEpochSecond(), instant.getNano());
    }

    public JsonNumber(final short number) {
        this.number = Short.toString(number);
    }

    @Override
    public JsonType type() {
        return JsonType.NUMBER;
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(number);
    }

    public BigInteger toBigInteger() {
        return new BigInteger(number);
    }

    public byte toByte() {
        return Byte.parseByte(number);
    }

    public double toDouble() {
        return Double.parseDouble(number);
    }

    public Duration toDuration() {
        final var number0 = Double.parseDouble(number);
        final long integer = (long) number0;
        return Duration.ofSeconds(integer, (long) ((number0 - integer) * 1e9));
    }

    public float toFloat() {
        return Float.parseFloat(number);
    }

    public int toInteger() {
        return Integer.parseInt(number);
    }

    public Instant toInstant() {
        final var number0 = Double.parseDouble(number);
        final long integer = (long) number0;
        return Instant.ofEpochSecond(integer, (long) ((number0 - integer) * 1e9));
    }

    public long toLong() {
        return Long.parseLong(number);
    }

    public OffsetDateTime toOffsetDateTime() {
        return OffsetDateTime.ofInstant(toInstant(), ZoneId.systemDefault());
    }

    public short toShort() {
        return Short.parseShort(number);
    }

    @Override
    public String toString() {
        return number;
    }

    public static JsonNumber readJson(final BinaryReader source) throws DtoReadException {
        return readJson(JsonTokenizer.tokenize(source));
    }

    @Internal
    public static JsonNumber readJson(final JsonTokenBuffer buffer) throws DtoReadException {
        final var source = buffer.source();
        var token = buffer.next();
        if (token.type() != JsonType.NUMBER) {
            throw new DtoReadException(DtoEncoding.JSON, "Expected number",
                token.readStringRaw(source), token.begin());
        }
        return new JsonNumber(token.readStringRaw(source));
    }

    @Override
    public void writeJson(final BinaryWriter writer) {
        writer.write(number.getBytes(StandardCharsets.ISO_8859_1));
    }
}
