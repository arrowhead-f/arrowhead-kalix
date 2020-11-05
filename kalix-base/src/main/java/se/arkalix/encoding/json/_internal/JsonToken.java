package se.arkalix.encoding.json._internal;

import se.arkalix.encoding.DecoderReadUnexpectedToken;
import se.arkalix.encoding.Encoding;
import se.arkalix.encoding.binary.BinaryReader;
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

    public BigDecimal readBigDecimal(final BinaryReader reader) {
        return new BigDecimal(readStringRaw(reader));
    }

    public BigInteger readBigInteger(final BinaryReader reader) {
        return new BigInteger(readStringRaw(reader));
    }

    public byte readByte(final BinaryReader reader) {
        return Byte.parseByte(requireNotHex(readStringRaw(reader)));
    }

    public double readDouble(final BinaryReader reader) {
        return Double.parseDouble(requireNotHex(readStringRaw(reader)));
    }

    public Duration readDuration(final BinaryReader reader) {
        return Duration.parse(readStringRaw(reader));
    }

    public Duration readDurationNumber(final BinaryReader reader) {
        final var number = Double.parseDouble(readStringRaw(reader));
        final long integer = (long) number;
        return Duration.ofSeconds(integer, (long) ((number - integer) * 1e9));
    }

    public float readFloat(final BinaryReader reader) {
        return Float.parseFloat(requireNotHex(readStringRaw(reader)));
    }

    public int readInteger(final BinaryReader reader) {
        return Integer.parseInt(requireNotHex(readStringRaw(reader)));
    }

    public Instant readInstant(final BinaryReader reader) {
        return Instant.parse(readStringRaw(reader));
    }

    public Instant readInstantNumber(final BinaryReader reader) {
        final var number = Double.parseDouble(readStringRaw(reader));
        final long integer = (long) number;
        return Instant.ofEpochSecond(integer, (long) ((number - integer) * 1e9));
    }

    public long readLong(final BinaryReader reader) {
        return Long.parseLong(requireNotHex(readStringRaw(reader)));
    }

    public MonthDay readMonthDay(final BinaryReader reader) {
        return MonthDay.parse(readStringRaw(reader));
    }

    public OffsetDateTime readOffsetDateTime(final BinaryReader reader) {
        return OffsetDateTime.parse(readStringRaw(reader));
    }

    public OffsetDateTime readOffsetDateTimeNumber(final BinaryReader reader) {
        return OffsetDateTime.ofInstant(readInstantNumber(reader), ZoneId.systemDefault());
    }

    public OffsetTime readOffsetTime(final BinaryReader reader) {
        return OffsetTime.parse(readStringRaw(reader));
    }

    public Period readPeriod(final BinaryReader reader) {
        return Period.parse(readStringRaw(reader));
    }

    public short readShort(final BinaryReader reader) {
        return Short.parseShort(requireNotHex(readStringRaw(reader)));
    }

    public String readString(final BinaryReader reader) {
        var p0 = begin; // Index of first non-appended byte in reader.
        var p1 = begin; // Current reader offset.
        final var p2 = end; // End of string reader region.

        final var buffer = new byte[p2 - p1];
        var b0 = 0; // Index of first unwritten byte in buffer.

        var badEscapeBuilder = new StringBuilder(0);
        error:
        {
            while (p1 < p2) {
                var b = reader.getByte(p1);
                if (b == '\\') {
                    // Collect bytes before escape sequence into buffer.
                    {
                        final var length = p1 - p0;
                        if (length > 0) {
                            reader.getBytes(p0, buffer, b0, length);
                            b0 += length;
                        }
                        p0 = ++p1;
                    }

                    if (p1 == p2) {
                        badEscapeBuilder.append('\\');
                        break error;
                    }

                    b = reader.getByte(p1++);
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
                                reader.getBytes(p1, uBuffer);
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
                            reader.getBytes(p1, uBuffer, 0, p2 - p1);
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
                reader.getBytes(p0, buffer, b0, length);
                b0 += length;
            }
            return new String(b0 < buffer.length
                ? Arrays.copyOfRange(buffer, 0, b0)
                : buffer,
                StandardCharsets.UTF_8);
        }
        throw new DecoderReadUnexpectedToken(Encoding.JSON, reader, badEscapeBuilder.toString(), p1, "Bad escape");
    }

    public String readStringRaw(final BinaryReader reader) {
        final var buffer = new byte[length()];
        reader.getBytes(begin, buffer);
        return new String(buffer, StandardCharsets.ISO_8859_1);
    }

    public Year readYear(final BinaryReader reader) {
        return Year.parse(readStringRaw(reader));
    }

    public Year readYearNumber(final BinaryReader reader) {
        final var number = Double.parseDouble(readStringRaw(reader));
        return Year.of((int) number);
    }

    public YearMonth readYearMonth(final BinaryReader reader) {
        return YearMonth.parse(readStringRaw(reader));
    }

    public ZonedDateTime readZonedDateTime(final BinaryReader reader) {
        return ZonedDateTime.parse(readStringRaw(reader));
    }

    public ZonedDateTime readZonedDateTimeNumber(final BinaryReader reader) {
        return ZonedDateTime.ofInstant(readInstantNumber(reader), ZoneOffset.UTC);
    }

    public ZoneId readZoneId(final BinaryReader reader) {
        return ZoneId.of(readStringRaw(reader));
    }

    public ZoneOffset readZoneOffset(final BinaryReader reader) {
        return ZoneOffset.of(readStringRaw(reader));
    }

    public ZoneOffset readZoneOffsetNumber(final BinaryReader reader) {
        final var number = Double.parseDouble(readStringRaw(reader));
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
