package se.arkalix.encoding.json._internal;

import se.arkalix.encoding.binary.BinaryReader;
import se.arkalix.encoding.json.JsonString;
import se.arkalix.encoding.json.JsonType;
import se.arkalix.util.annotation.Internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Arrays;

@Internal
@SuppressWarnings("unused")
public final class JsonToken {
    final JsonType type;

    int begin, end;
    int nChildren;

    public JsonType type() {
        return type;
    }

    public int begin() {
        return begin;
    }

    public int end() {
        return end;
    }

    public int length() {
        return end - begin;
    }

    public int nChildren() {
        return nChildren;
    }

    JsonToken(final JsonType type, final int begin, final int end, final int nChildren) {
        assert begin >= 0 && begin < end && nChildren >= 0;

        this.type = type;
        this.begin = begin;
        this.end = end;
        this.nChildren = nChildren;
    }

    public BigDecimal readBigDecimal(final BinaryReader source) {
        return new BigDecimal(readStringRaw(source));
    }

    public BigInteger readBigInteger(final BinaryReader source) {
        return new BigInteger(readStringRaw(source));
    }

    public byte readByte(final BinaryReader source) {
        return Byte.parseByte(requireNotHex(readStringRaw(source)));
    }

    public double readDouble(final BinaryReader source) {
        return Double.parseDouble(requireNotHex(readStringRaw(source)));
    }

    public Duration readDuration(final BinaryReader source) {
        return Duration.parse(readStringRaw(source));
    }

    public Duration readDurationNumber(final BinaryReader source) {
        final var number = Double.parseDouble(readStringRaw(source));
        final long integer = (long) number;
        return Duration.ofSeconds(integer, (long) ((number - integer) * 1e9));
    }

    public float readFloat(final BinaryReader source) {
        return Float.parseFloat(requireNotHex(readStringRaw(source)));
    }

    public int readInteger(final BinaryReader source) {
        return Integer.parseInt(requireNotHex(readStringRaw(source)));
    }

    public Instant readInstant(final BinaryReader source) {
        return Instant.parse(readStringRaw(source));
    }

    public Instant readInstantNumber(final BinaryReader source) {
        final var number = Double.parseDouble(readStringRaw(source));
        final long integer = (long) number;
        return Instant.ofEpochSecond(integer, (long) ((number - integer) * 1e9));
    }

    public long readLong(final BinaryReader source) {
        return Long.parseLong(requireNotHex(readStringRaw(source)));
    }

    public MonthDay readMonthDay(final BinaryReader source) {
        return MonthDay.parse(readStringRaw(source));
    }

    public OffsetDateTime readOffsetDateTime(final BinaryReader source) {
        return OffsetDateTime.parse(readStringRaw(source));
    }

    public OffsetDateTime readOffsetDateTimeNumber(final BinaryReader source) {
        return OffsetDateTime.ofInstant(readInstantNumber(source), ZoneId.systemDefault());
    }

    public OffsetTime readOffsetTime(final BinaryReader source) {
        return OffsetTime.parse(readStringRaw(source));
    }

    public Period readPeriod(final BinaryReader source) {
        return Period.parse(readStringRaw(source));
    }

    public short readShort(final BinaryReader source) {
        return Short.parseShort(requireNotHex(readStringRaw(source)));
    }

    public String readString(final BinaryReader source) throws DtoReadException {
        var p0 = begin; // Index of first non-appended byte in source.
        var p1 = begin; // Current source offset.
        final var p2 = end; // End of string source region.

        final var buffer = new byte[p2 - p1];
        var b0 = 0; // Index of first unwritten byte in buffer.

        var badEscapeBuilder = new StringBuilder(0);
        error:
        {
            while (p1 < p2) {
                var b = source.getByte(p1);
                if (b == '\\') {
                    // Collect bytes before escape sequence into buffer.
                    {
                        final var length = p1 - p0;
                        if (length > 0) {
                            source.getBytes(p0, buffer, b0, length);
                            b0 += length;
                        }
                        p0 = ++p1;
                    }

                    if (p1 == p2) {
                        badEscapeBuilder.append('\\');
                        break error;
                    }

                    b = source.getByte(p1++);
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
                                source.getBytes(p1, uBuffer);
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
                            source.getBytes(p1, uBuffer, 0, p2 - p1);
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
                source.getBytes(p0, buffer, b0, length);
                b0 += length;
            }
            return new String(b0 < buffer.length
                ? Arrays.copyOfRange(buffer, 0, b0)
                : buffer,
                StandardCharsets.UTF_8);
        }
        throw new DtoReadException(JsonString.class, DtoEncoding.JSON,
            "Bad escape", badEscapeBuilder.toString(), p1);
    }

    public String readStringRaw(final BinaryReader source) {
        final var buffer = new byte[length()];
        source.getBytes(begin, buffer);
        return new String(buffer, StandardCharsets.ISO_8859_1);
    }

    public Year readYear(final BinaryReader source) {
        return Year.parse(readStringRaw(source));
    }

    public Year readYearNumber(final BinaryReader source) {
        final var number = Double.parseDouble(readStringRaw(source));
        return Year.of((int) number);
    }

    public YearMonth readYearMonth(final BinaryReader source) {
        return YearMonth.parse(readStringRaw(source));
    }

    public ZonedDateTime readZonedDateTime(final BinaryReader source) {
        return ZonedDateTime.parse(readStringRaw(source));
    }

    public ZonedDateTime readZonedDateTimeNumber(final BinaryReader source) {
        return ZonedDateTime.ofInstant(readInstantNumber(source), ZoneOffset.UTC);
    }

    public ZoneId readZoneId(final BinaryReader source) {
        return ZoneId.of(readStringRaw(source));
    }

    public ZoneOffset readZoneOffset(final BinaryReader source) {
        return ZoneOffset.of(readStringRaw(source));
    }

    public ZoneOffset readZoneOffsetNumber(final BinaryReader source) {
        final var number = Double.parseDouble(readStringRaw(source));
        return ZoneOffset.ofTotalSeconds((int) number);
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
}
