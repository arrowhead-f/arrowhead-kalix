package se.arkalix.dto.json._internal;

import se.arkalix.dto.DtoWriteException;
import se.arkalix.dto.binary.BinaryWriter;
import se.arkalix.dto.json.JsonEncoding;
import se.arkalix.util.annotation.Internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.*;

@Internal
@SuppressWarnings("unused")
public final class JsonWrite {
    private JsonWrite() {}

    private static final byte[] TRUE = new byte[]{'t', 'r', 'u', 'e'};
    private static final byte[] FALSE = new byte[]{'f', 'a', 'l', 's', 'e'};
    private static final byte[] HEX = new byte[]{
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static void write(final BigDecimal bigDecimal, final BinaryWriter target) {
        target.write(bigDecimal.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final BigInteger bigInteger, final BinaryWriter target) {
        target.write(bigInteger.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final boolean bool, final BinaryWriter target) {
        target.write(bool ? TRUE : FALSE);
    }

    public static void write(final Duration duration, final BinaryWriter target) {
        target.write(duration.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final Instant instant, final BinaryWriter target) {
        target.write(instant.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final MonthDay monthDay, final BinaryWriter target) {
        target.write(monthDay.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final long number, final BinaryWriter target) {
        target.write(Long.toString(number)
            .getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final double number, final BinaryWriter target) throws DtoWriteException {
        if (!Double.isFinite(number)) {
            throw new DtoWriteException(JsonEncoding.instance(), number, "" +
                "NaN, +Infinify and -Infinity cannot be represented as JSON");
        }
        target.write(Double.toString(number)
            .getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final OffsetDateTime offsetDateTime, final BinaryWriter target) {
        target.write(offsetDateTime.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final OffsetTime offsetTime, final BinaryWriter target) {
        target.write(offsetTime.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final Period period, final BinaryWriter target) {
        target.write(period.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final String string, final BinaryWriter target) {
        for (var b : string.getBytes(StandardCharsets.UTF_8)) {
            if (b >= 0 && b < ' ' || b == '"') {
                target.write((byte) '\\');
                switch (b) {
                case '\b': b = 'b'; break;
                case '\f': b = 'f'; break;
                case '\n': b = 'n'; break;
                case '\r': b = 'r'; break;
                case '"': break;
                default:
                    target.write(new byte[]{'u', '0', '0'});
                    target.write(HEX[(b & 0xF0) >>> 4]);
                    target.write(HEX[(b & 0x0F)]);
                    continue;
                }
            }
            target.write(b);
        }
    }

    public static void write(final Year year, final BinaryWriter target) {
        target.write(("" + year.getValue()).getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final YearMonth yearMonth, final BinaryWriter target) {
        target.write(yearMonth.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final ZonedDateTime zonedDateTime, final BinaryWriter target) {
        target.write(zonedDateTime.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final ZoneId zoneId, final BinaryWriter target) {
        target.write(zoneId.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final ZoneOffset zoneOffset, final BinaryWriter target) {
        target.write(zoneOffset.toString().getBytes(StandardCharsets.ISO_8859_1));
    }
}
