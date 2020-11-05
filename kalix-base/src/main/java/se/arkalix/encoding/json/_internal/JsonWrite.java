package se.arkalix.encoding.json._internal;

import se.arkalix.encoding.Encodable;
import se.arkalix.encoding.Encoding;
import se.arkalix.encoding.EncodableConstraintViolated;
import se.arkalix.encoding.binary.BinaryWriter;
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

    public static void write(final BigDecimal bigDecimal, final BinaryWriter writer) {
        writer.write(bigDecimal.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final BigInteger bigInteger, final BinaryWriter writer) {
        writer.write(bigInteger.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final boolean bool, final BinaryWriter writer) {
        writer.write(bool ? TRUE : FALSE);
    }

    public static void write(final Duration duration, final BinaryWriter writer) {
        writer.write(duration.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final Instant instant, final BinaryWriter writer) {
        writer.write(instant.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final MonthDay monthDay, final BinaryWriter writer) {
        writer.write(monthDay.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final long number, final BinaryWriter writer) {
        writer.write(Long.toString(number)
            .getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final double number, final BinaryWriter writer)
        throws EncodableConstraintViolated
    {
        if (!Double.isFinite(number)) {
            throw new EncodableConstraintViolated(
                Encoding.JSON,
                writer,
                number,
                "NaN, +Infinify and -Infinity cannot be represented"
            );
        }
        writer.write(Double.toString(number)
            .getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final OffsetDateTime offsetDateTime, final BinaryWriter writer) {
        writer.write(offsetDateTime.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final OffsetTime offsetTime, final BinaryWriter writer) {
        writer.write(offsetTime.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final Period period, final BinaryWriter writer) {
        writer.write(period.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final String string, final BinaryWriter writer) {
        final var bytes = string.getBytes(StandardCharsets.UTF_8);

        for (var i = 0; i < bytes.length; ++i) {
            var b = bytes[i];
            if (b >= 0 && b < ' ' || b == '"' || b == 0x7F) {
                writer.write((byte) '\\');
                switch (b) {
                case '\b': b = 'b'; break;
                case '\f': b = 'f'; break;
                case '\n': b = 'n'; break;
                case '\r': b = 'r'; break;
                case '"': break;
                default:
                    writer.write(new byte[]{'u', '0', '0'});
                    writer.write(HEX[(b & 0xF0) >>> 4]);
                    writer.write(HEX[(b & 0x0F)]);
                    continue;
                }
            }
            if (b == (byte) 0xC2) {
                b = bytes[++i];
                if (b <= (byte) 0x9F) {
                    writer.write(new byte[]{'u', '0', '0'});
                    writer.write(HEX[(b & 0xF0) >>> 4]);
                    writer.write(HEX[(b & 0x0F)]);
                    continue;
                }
                else {
                    writer.write((byte) 0xC2);
                }
            }
            writer.write(b);
        }
    }

    public static void write(final Year year, final BinaryWriter writer) {
        writer.write(("" + year.getValue()).getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final YearMonth yearMonth, final BinaryWriter writer) {
        writer.write(yearMonth.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final ZonedDateTime zonedDateTime, final BinaryWriter writer) {
        writer.write(zonedDateTime.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final ZoneId zoneId, final BinaryWriter writer) {
        writer.write(zoneId.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final ZoneOffset zoneOffset, final BinaryWriter writer) {
        writer.write(zoneOffset.toString().getBytes(StandardCharsets.ISO_8859_1));
    }
}
