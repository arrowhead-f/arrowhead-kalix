package se.arkalix.codec.json._internal;

import se.arkalix.codec.CodecType;
import se.arkalix.codec.DecoderException;
import se.arkalix.codec.EncodableException;
import se.arkalix.io.buf.BufferReader;
import se.arkalix.io.buf.BufferWriter;
import se.arkalix.util.annotation.Internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Arrays;

@Internal
@SuppressWarnings("unused")
public final class JsonPrimitives {
    private JsonPrimitives() {}

    private static final byte[] TRUE = new byte[]{'t', 'r', 'u', 'e'};
    private static final byte[] FALSE = new byte[]{'f', 'a', 'l', 's', 'e'};
    private static final byte[] HEX = new byte[]{
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static BigDecimal readBigDecimal(final JsonToken token, final BufferReader reader) {
        return new BigDecimal(readStringRaw(token, reader));
    }

    public static BigInteger readBigInteger(final JsonToken token, final BufferReader reader) {
        return new BigInteger(readStringRaw(token, reader));
    }

    public static byte readByte(final JsonToken token, final BufferReader reader) {
        return Byte.parseByte(requireNotHex(readStringRaw(token, reader)));
    }

    private static String requireNotHex(final String string) {
        if (string.length() > 2 && string.charAt(0) == '0') {
            final var x = string.charAt(1);
            if (x == 'x' || x == 'X') {
                throw new NumberFormatException("Unexpected x");
            }
        }
        return string;
    }

    public static char readChar(final JsonToken token, final BufferReader reader) {
        var c0 = token.begin();
        final var c1 = token.end();
        String error;
        error:
        {
            var b = reader.getS8At(c0);
            if (b < 0) {
                if (b < -64) {
                    error = "bad JSON character string; invalid UTF-8 " +
                        "lead byte";
                    break error;
                }
                if (b < -32) { // 2 byte UTF-8 code point.
                    if (c0 + 2 != c1) {
                        error = "bad JSON character string; must contain " +
                            "exactly one valid UTF-8 code point";
                        break error;
                    }
                    return (char) (((0x1F & b) << 6) |
                        (0x3F & reader.getS8At(c0 + 1)));
                }
                if (b < -16) { // 3 byte UTF-8 code point.
                    if (c0 + 3 != c1) {
                        error = "bad JSON character string; must contain " +
                            "exactly one valid UTF-8 code point";
                        break error;
                    }
                    return (char) (((0x0F & b) << 12) |
                        ((0x3F & reader.getS8At(c0 + 1)) << 6) |
                        (0x3F & reader.getS8At(c0 + 2)));
                }
                error = "bad JSON character string; 4-byte UTF-8 code " +
                    "points do not fit in a java char";
                break error;
            }
            if (b < 0x20 || b == 0x7F) {
                error = "bad JSON character string; Unicode basic latin " +
                    "control characters not allowed in JSON strings";
                break error;
            }
            if (b == '\\') {
                b = reader.getS8At(++c0);
                switch (b) {
                case '\"':
                case '/':
                case '\\':
                    break;

                case 'b': b = '\b'; break;
                case 'f': b = '\f'; break;
                case 'r': b = '\r'; break;
                case 'n': b = '\n'; break;
                case 't': b = '\t'; break;

                case 'u':
                    final var uBuffer = new byte[4];
                    if (++c0 + 4 == c1) {
                        try {
                            reader.getAt(c0, uBuffer);
                            final var uString = new String(uBuffer, StandardCharsets.ISO_8859_1);
                            final var uNumber = Integer.parseUnsignedInt(uString, 16);
                            return (char) uNumber;
                        }
                        catch (final NumberFormatException ignored) {}
                    }
                default:
                    error = "bad JSON character string; bad escape sequence";
                    break error;
                }
            }
            if (c0 + 1 == c1) {
                return (char) b;
            }
            error = "bad JSON character string; must contain exactly one " +
                "valid character or escape sequence";
        }
        throw new DecoderException(
            CodecType.JSON,
            reader,
            readStringRaw(token, reader),
            token.begin(),
            error);
    }

    public static double readDouble(final JsonToken token, final BufferReader reader) {
        return Double.parseDouble(requireNotHex(readStringRaw(token, reader)));
    }

    public static Duration readDuration(final JsonToken token, final BufferReader reader) {
        return Duration.parse(readStringRaw(token, reader));
    }

    public static Duration readDurationNumber(final JsonToken token, final BufferReader reader) {
        final var number = Double.parseDouble(readStringRaw(token, reader));
        final long integer = (long) number;
        return Duration.ofSeconds(integer, (long) ((number - integer) * 1e9));
    }

    public static float readFloat(final JsonToken token, final BufferReader reader) {
        return Float.parseFloat(requireNotHex(readStringRaw(token, reader)));
    }

    public static int readInteger(final JsonToken token, final BufferReader reader) {
        return Integer.parseInt(requireNotHex(readStringRaw(token, reader)));
    }

    public static Instant readInstant(final JsonToken token, final BufferReader reader) {
        return Instant.parse(readStringRaw(token, reader));
    }

    public static Instant readInstantNumber(final JsonToken token, final BufferReader reader) {
        final var number = Double.parseDouble(readStringRaw(token, reader));
        final long integer = (long) number;
        return Instant.ofEpochSecond(integer, (long) ((number - integer) * 1e9));
    }

    public static long readLong(final JsonToken token, final BufferReader reader) {
        return Long.parseLong(requireNotHex(readStringRaw(token, reader)));
    }

    public static MonthDay readMonthDay(final JsonToken token, final BufferReader reader) {
        return MonthDay.parse(readStringRaw(token, reader));
    }

    public static OffsetDateTime readOffsetDateTime(final JsonToken token, final BufferReader reader) {
        return OffsetDateTime.parse(readStringRaw(token, reader));
    }

    public static OffsetDateTime readOffsetDateTimeNumber(final JsonToken token, final BufferReader reader) {
        return OffsetDateTime.ofInstant(readInstantNumber(token, reader), ZoneId.systemDefault());
    }

    public static OffsetTime readOffsetTime(final JsonToken token, final BufferReader reader) {
        return OffsetTime.parse(readStringRaw(token, reader));
    }

    public static Period readPeriod(final JsonToken token, final BufferReader reader) {
        return Period.parse(readStringRaw(token, reader));
    }

    public static short readShort(final JsonToken token, final BufferReader reader) {
        return Short.parseShort(requireNotHex(readStringRaw(token, reader)));
    }

    public static String readString(final JsonToken token, final BufferReader reader) {
        var p0 = token.begin(); // Index of first non-appended byte in reader.
        var p1 = p0; // Current reader offset.
        final var p2 = token.end(); // End of string reader region.

        final var buffer = new byte[p2 - p1];
        var b0 = 0; // Index of first unwritten byte in buffer.

        var badEscapeBuilder = new StringBuilder(0);
        error:
        {
            while (p1 < p2) {
                var b = reader.getS8At(p1);
                if (b == '\\') {
                    // Collect bytes before escape sequence into buffer.
                    {
                        final var length = p1 - p0;
                        if (length > 0) {
                            reader.getAt(p0, buffer, b0, length);
                            b0 += length;
                        }
                        p0 = ++p1;
                    }

                    if (p1 == p2) {
                        badEscapeBuilder.append('\\');
                        break error;
                    }

                    b = reader.getS8At(p1++);
                    switch (b) {
                    case '\"':
                    case '/':
                    case '\\':
                        break;

                    case 'b': b = '\b'; break;
                    case 'f': b = '\f'; break;
                    case 'r': b = '\r'; break;
                    case 'n': b = '\n'; break;
                    case 't': b = '\t'; break;

                    case 'u':
                        final var uBuffer = new byte[4];
                        if (p1 + 4 <= p2) {
                            try {
                                reader.getAt(p1, uBuffer);
                                final var uString = new String(uBuffer, StandardCharsets.ISO_8859_1);
                                final var uNumber = Integer.parseUnsignedInt(uString, 16);
                                final var uBytes = Character.toString(uNumber)
                                    .getBytes(StandardCharsets.UTF_8);
                                for (byte uByte : uBytes) {
                                    buffer[b0++] = uByte;
                                }
                                p0 += 5;
                                p1 += 4;
                                continue;
                            }
                            catch (final NumberFormatException ignored) {}
                        }
                        else {
                            reader.getAt(p1, uBuffer, 0, p2 - p1);
                        }
                        badEscapeBuilder.append("\\u").append(new String(uBuffer, StandardCharsets.US_ASCII));
                        break error;

                    default:
                        badEscapeBuilder.append('\\').append(Character.toString(b));
                        break error;
                    }
                    buffer[b0++] = b;
                    p0++;
                }
                else {
                    p1++;
                }
            }
            final var length = p1 - p0;
            if (length > 0) {
                reader.getAt(p0, buffer, b0, length);
                b0 += length;
            }
            return new String(b0 < buffer.length
                ? Arrays.copyOfRange(buffer, 0, b0)
                : buffer,
                StandardCharsets.UTF_8);
        }
        throw new DecoderException(CodecType.JSON, reader, badEscapeBuilder.toString(), p1, "bad escape");
    }

    public static String readStringRaw(final JsonToken token, final BufferReader reader) {
        final var buffer = new byte[token.length()];
        reader.getAt(token.begin(), buffer);
        return new String(buffer, StandardCharsets.ISO_8859_1);
    }

    public static Year readYear(final JsonToken token, final BufferReader reader) {
        return Year.parse(readStringRaw(token, reader));
    }

    public static Year readYearNumber(final JsonToken token, final BufferReader reader) {
        final var number = Double.parseDouble(readStringRaw(token, reader));
        return Year.of((int) number);
    }

    public static YearMonth readYearMonth(final JsonToken token, final BufferReader reader) {
        return YearMonth.parse(readStringRaw(token, reader));
    }

    public static ZonedDateTime readZonedDateTime(final JsonToken token, final BufferReader reader) {
        return ZonedDateTime.parse(readStringRaw(token, reader));
    }

    public static ZonedDateTime readZonedDateTimeNumber(final JsonToken token, final BufferReader reader) {
        return ZonedDateTime.ofInstant(readInstantNumber(token, reader), ZoneOffset.UTC);
    }

    public static ZoneId readZoneId(final JsonToken token, final BufferReader reader) {
        return ZoneId.of(readStringRaw(token, reader));
    }

    public static ZoneOffset readZoneOffset(final JsonToken token, final BufferReader reader) {
        return ZoneOffset.of(readStringRaw(token, reader));
    }

    public static ZoneOffset readZoneOffsetNumber(final JsonToken token, final BufferReader reader) {
        final var number = Double.parseDouble(readStringRaw(token, reader));
        return ZoneOffset.ofTotalSeconds((int) number);
    }

    public static void write(final BigDecimal bigDecimal, final BufferWriter writer) {
        writer.write(bigDecimal.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final BigInteger bigInteger, final BufferWriter writer) {
        writer.write(bigInteger.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final boolean bool, final BufferWriter writer) {
        writer.write(bool ? TRUE : FALSE);
    }

    public static void write(final char ch, final BufferWriter writer) {
        write(Character.toString(ch), writer);
    }

    public static void write(final Duration duration, final BufferWriter writer) {
        writer.write(duration.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final Instant instant, final BufferWriter writer) {
        writer.write(instant.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final MonthDay monthDay, final BufferWriter writer) {
        writer.write(monthDay.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final long number, final BufferWriter writer) {
        writer.write(Long.toString(number)
            .getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final double number, final BufferWriter writer)
        throws EncodableException {
        if (!Double.isFinite(number)) {
            throw new EncodableException(
                CodecType.JSON,
                writer,
                number,
                "NaN, +Infinify and -Infinity cannot be represented"
            );
        }
        writer.write(Double.toString(number)
            .getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final OffsetDateTime offsetDateTime, final BufferWriter writer) {
        writer.write(offsetDateTime.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final OffsetTime offsetTime, final BufferWriter writer) {
        writer.write(offsetTime.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final Period period, final BufferWriter writer) {
        writer.write(period.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final String string, final BufferWriter writer) {
        final var bytes = string.getBytes(StandardCharsets.UTF_8);

        for (var i = 0; i < bytes.length; ++i) {
            var b = bytes[i];
            if (b >= 0 && b < ' ' || b == '"' || b == 0x7F) {
                writer.writeS8((byte) '\\');
                switch (b) {
                case '\b': b = 'b'; break;
                case '\f': b = 'f'; break;
                case '\n': b = 'n'; break;
                case '\r': b = 'r'; break;
                case '"': break;
                default:
                    writer.write(new byte[]{'u', '0', '0'});
                    writer.writeS8(HEX[(b & 0xF0) >>> 4]);
                    writer.writeS8(HEX[(b & 0x0F)]);
                    continue;
                }
            }
            if (b == (byte) 0xC2) {
                b = bytes[++i];
                if (b <= (byte) 0x9F) {
                    writer.write(new byte[]{'u', '0', '0'});
                    writer.writeS8(HEX[(b & 0xF0) >>> 4]);
                    writer.writeS8(HEX[(b & 0x0F)]);
                    continue;
                }
                else {
                    writer.writeS8((byte) 0xC2);
                }
            }
            writer.writeS8(b);
        }
    }

    public static void write(final Year year, final BufferWriter writer) {
        writer.write(("" + year.getValue()).getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final YearMonth yearMonth, final BufferWriter writer) {
        writer.write(yearMonth.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final ZonedDateTime zonedDateTime, final BufferWriter writer) {
        writer.write(zonedDateTime.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final ZoneId zoneId, final BufferWriter writer) {
        writer.write(zoneId.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final ZoneOffset zoneOffset, final BufferWriter writer) {
        writer.write(zoneOffset.toString().getBytes(StandardCharsets.ISO_8859_1));
    }
}
