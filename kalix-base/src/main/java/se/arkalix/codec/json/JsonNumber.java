package se.arkalix.codec.json;

import se.arkalix.codec.CodecType;
import se.arkalix.codec.DecoderReadUnexpectedToken;
import se.arkalix.codec.binary.BinaryReader;
import se.arkalix.codec.binary.BinaryWriter;
import se.arkalix.codec.json._internal.JsonPrimitives;
import se.arkalix.codec.json._internal.JsonTokenBuffer;
import se.arkalix.codec.json._internal.JsonTokenizer;
import se.arkalix.util.annotation.Internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Objects;
import java.util.Optional;

/**
 * JSON number.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
@SuppressWarnings("unused")
public class JsonNumber implements JsonValue {
    private final String number;

    private JsonNumber(final String number) {
        this.number = number;
    }

    /**
     * Creates new JSON number from given {@link BigDecimal}.
     *
     * @param number Number.
     */
    public JsonNumber(final BigDecimal number) {
        this.number = Objects.requireNonNull(number, "number").toString();
    }

    /**
     * Creates new JSON number from given {@link BigInteger}.
     *
     * @param number Number.
     */
    public JsonNumber(final BigInteger number) {
        this.number = Objects.requireNonNull(number, "number").toString();
    }

    /**
     * Creates new JSON number from given {@code byte}.
     *
     * @param number Number.
     */
    public JsonNumber(final byte number) {
        this.number = Byte.toString(number);
    }

    /**
     * Creates new JSON number from given {@code double}.
     *
     * @param number Number.
     */
    public JsonNumber(final double number) {
        this.number = Double.toString(number);
    }

    /**
     * Creates new JSON number from given {@link Duration}.
     *
     * @param number Number.
     */
    public JsonNumber(final Duration number) {
        Objects.requireNonNull(number, "number");
        this.number = formatDecimal(number.getSeconds(), number.toNanosPart());
    }

    /**
     * Creates new JSON number from given {@code float}.
     *
     * @param number Number.
     */
    public JsonNumber(final float number) {
        this.number = Float.toString(number);
    }

    /**
     * Creates new JSON number from given {@code int}.
     *
     * @param number Number.
     */
    public JsonNumber(final int number) {
        this.number = Integer.toString(number);
    }

    /**
     * Creates new JSON number from given {@link Instant}.
     *
     * @param number Number.
     */
    public JsonNumber(final Instant number) {
        Objects.requireNonNull(number, "number");
        this.number = formatDecimal(number.getEpochSecond(), number.getNano());
    }

    /**
     * Creates new JSON number from given {@code long}.
     *
     * @param number Number.
     */
    public JsonNumber(final long number) {
        this.number = Long.toString(number);
    }

    /**
     * Creates new JSON number from given {@link OffsetDateTime}.
     *
     * @param number Number.
     */
    public JsonNumber(final OffsetDateTime number) {
        final var instant = Objects.requireNonNull(number, "number").toInstant();
        this.number = formatDecimal(instant.getEpochSecond(), instant.getNano());
    }

    /**
     * Creates new JSON number from given {@code short}.
     *
     * @param number Number.
     */
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

    /**
     * @return This number converted to a {@link BigDecimal}.
     */
    public BigDecimal toBigDecimal() {
        return new BigDecimal(number);
    }

    /**
     * @return This number converted to a {@link BigInteger}.
     * @throws NumberFormatException If this number contains decimals or uses
     *                               exponent notation.
     */
    public BigInteger toBigInteger() {
        return new BigInteger(number);
    }

    /**
     * @return This number converted to a {@code byte}.
     * @throws NumberFormatException If this number contains decimals, uses
     *                               exponent notation or is too large.
     */
    public byte toByte() {
        return Byte.parseByte(number);
    }

    /**
     * @return This number converted to a {@code double}.
     */
    public double toDouble() {
        return Double.parseDouble(number);
    }

    @Override
    public Optional<Double> tryToDouble() {
        return Optional.of(toDouble());
    }

    /**
     * @return This number converted to a {@link Duration}.
     * @throws ArithmeticException If this number is too large.
     */
    public Duration toDuration() {
        final var number0 = Double.parseDouble(number);
        final long integer = (long) number0;
        return Duration.ofSeconds(integer, (long) ((number0 - integer) * 1e9));
    }

    /**
     * @return This number converted to a {@code float}.
     */
    public float toFloat() {
        return Float.parseFloat(number);
    }

    /**
     * @return This number converted to a {@code int}.
     * @throws NumberFormatException If this number contains decimals, uses
     *                               exponent notation or is too large.
     */
    public int toInteger() {
        return Integer.parseInt(number);
    }

    /**
     * @return This number converted to a {@link Instant}.
     * @throws DateTimeException   If this number is too large to be represented
     *                             by an {@link Instant}.
     * @throws ArithmeticException If this number is too large.
     */
    public Instant toInstant() {
        final var number0 = Double.parseDouble(number);
        final long integer = (long) number0;
        return Instant.ofEpochSecond(integer, (long) ((number0 - integer) * 1e9));
    }

    /**
     * @return This number converted to a {@code long}.
     * @throws NumberFormatException If this number contains decimals, uses
     *                               exponent notation or is too large.
     */
    public long toLong() {
        return Long.parseLong(number);
    }

    @Override
    public Optional<Long> tryToLong() {
        return Optional.of(toLong());
    }

    /**
     * @return This number converted to an {@link OffsetDateTime}.
     * @throws DateTimeException   If this number is too large to be represented
     *                             by an {@link OffsetDateTime}.
     * @throws ArithmeticException If this number is too large.
     */
    public OffsetDateTime toOffsetDateTime() {
        return OffsetDateTime.ofInstant(toInstant(), ZoneId.systemDefault());
    }

    /**
     * @return This number converted to a {@code short}.
     * @throws NumberFormatException If this number contains decimals, uses
     *                               exponent notation or is too large.
     */
    public short toShort() {
        return Short.parseShort(number);
    }

    /**
     * Reads JSON number from given {@code reader}.
     *
     * @param reader Source containing JSON number at the current read offset,
     *               ignoring any whitespace.
     * @return Decoded JSON number.
     * @throws DecoderReadUnexpectedToken If the reader does not contain a
     *                                    valid JSON number at the current read
     *                                    offset.
     */
    public static JsonNumber readJson(final BinaryReader reader) {
        return readJson(JsonTokenizer.tokenize(reader));
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
    @Internal
    public static JsonNumber readJson(final JsonTokenBuffer buffer) {
        final var reader = buffer.reader();
        final var token = buffer.next();
        final var string = JsonPrimitives.readStringRaw(token, reader);
        if (token.type() != JsonType.NUMBER) {
            throw new DecoderReadUnexpectedToken(
                CodecType.JSON,
                reader,
                string,
                token.begin(),
                "expected number");
        }
        return new JsonNumber(string);
    }

    @Override
    public CodecType writeJson(final BinaryWriter writer) {
        writer.write(number.getBytes(StandardCharsets.ISO_8859_1));
        return CodecType.JSON;
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
