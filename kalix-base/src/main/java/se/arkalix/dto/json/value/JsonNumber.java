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
import java.util.Objects;

@SuppressWarnings("unused")
public class JsonNumber implements JsonValue {
    private final String number;

    private JsonNumber(final String number) {
        this.number = number;
    }

    public JsonNumber(final BigDecimal number) {
        this.number = Objects.requireNonNull(number, "Expected number").toString();
    }

    public JsonNumber(final BigInteger number) {
        this.number = Objects.requireNonNull(number, "Expected number").toString();
    }

    public JsonNumber(final byte number) {
        this.number = Byte.toString(number);
    }

    public JsonNumber(final double number) {
        this.number = Double.toString(number);
    }

    public JsonNumber(final Duration number) {
        Objects.requireNonNull(number, "Expected number");
        this.number = formatDecimal(number.getSeconds(), number.toNanosPart());
    }

    public JsonNumber(final float number) {
        this.number = Float.toString(number);
    }

    public JsonNumber(final int number) {
        this.number = Integer.toString(number);
    }

    public JsonNumber(final Instant number) {
        Objects.requireNonNull(number, "Expected number");
        this.number = formatDecimal(number.getEpochSecond(), number.getNano());
    }

    public JsonNumber(final long number) {
        this.number = Long.toString(number);
    }

    public JsonNumber(final OffsetDateTime number) {
        final var instant = Objects.requireNonNull(number, "Expected number").toInstant();
        this.number = formatDecimal(instant.getEpochSecond(), instant.getNano());
    }

    public JsonNumber(final short number) {
        this.number = Short.toString(number);
    }

    private static String formatDecimal(final long seconds, final int nanos) {
        final var builder = new StringBuilder().append(seconds);
        if (nanos == 0) {
            return builder.toString();
        }
        builder.append('.');
        final var n = Integer.toString(nanos);
        var n1 = n.length();
        for (var padLeft = 9 - n1; padLeft-- != 0; ) {
            builder.append('0');
        }
        while (n1 > 0 && n.charAt(n1 - 1) == '0') {
            n1 -= 1;
        }
        for (var n0 = 0; n0 < n1; ++n0) {
            builder.append(n.charAt(n0));
        }
        return builder.toString();
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

    public static JsonNumber readJson(final BinaryReader source) throws DtoReadException {
        return readJson(JsonTokenizer.tokenize(source));
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
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

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final JsonNumber that = (JsonNumber) other;
        return number.equals(that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return number;
    }
}
