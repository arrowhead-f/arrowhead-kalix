package eu.arrowhead.kalix.dto.json;

import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.dto.ReadException;
import eu.arrowhead.kalix.dto.binary.BinaryReader;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.*;

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
        return new BigDecimal(readRawString(source));
    }

    public BigInteger readBigInteger(final BinaryReader source) {
        return new BigInteger(readRawString(source));
    }

    public byte readByte(final BinaryReader source) {
        return Byte.parseByte(requireNotHex(readRawString(source)));
    }

    public double readDouble(final BinaryReader source) {
        return Double.parseDouble(requireNotHex(readRawString(source)));
    }

    public Duration readDuration(final BinaryReader source) {
        return Duration.parse(readRawString(source));
    }

    public Duration readDurationNumber(final BinaryReader source) {
        final var number = Double.parseDouble(readRawString(source));
        return Duration.ofNanos((long) (number * 1e9));
    }

    public float readFloat(final BinaryReader source) {
        return Float.parseFloat(requireNotHex(readRawString(source)));
    }

    public int readInteger(final BinaryReader source) {
        return Integer.parseInt(requireNotHex(readRawString(source)));
    }

    public Instant readInstant(final BinaryReader source) {
        return Instant.parse(readRawString(source));
    }

    public Instant readInstantNumber(final BinaryReader source) {
        final var number = Double.parseDouble(readRawString(source));
        final long integer = (long) number;
        return Instant.ofEpochSecond(integer, (long) ((number - integer) * 1e9));
    }

    public LocalDate readLocalDate(final BinaryReader source) {
        return LocalDate.parse(readRawString(source));
    }

    public LocalDate readLocalDateNumber(final BinaryReader source) {
        final var number = Double.parseDouble(readRawString(source));
        return LocalDate.ofEpochDay((long) number);
    }

    public LocalDateTime readLocalDateTime(final BinaryReader source) {
        return LocalDateTime.parse(readRawString(source));
    }

    public LocalDateTime readLocalDateTimeNumber(final BinaryReader source) {
        final var number = Double.parseDouble(readRawString(source));
        final long integer = (long) number;
        return LocalDateTime.ofEpochSecond(integer, (int) ((number - integer) * 1e9), ZoneOffset.UTC);
    }

    public LocalTime readLocalTime(final BinaryReader source) {
        return LocalTime.parse(readRawString(source));
    }

    public LocalTime readLocalTimeNumber(final BinaryReader source) {
        final var number = Double.parseDouble(readRawString(source));
        return LocalTime.ofNanoOfDay((long) (number * 1e9));
    }

    public long readLong(final BinaryReader source) {
        return Long.parseLong(requireNotHex(readRawString(source)));
    }

    public MonthDay readMonthDay(final BinaryReader source) {
        return MonthDay.parse(readRawString(source));
    }

    public OffsetDateTime readOffsetDateTime(final BinaryReader source) {
        return OffsetDateTime.parse(readRawString(source));
    }

    public OffsetDateTime readOffsetDateTimeNumber(final BinaryReader source) {
        return OffsetDateTime.ofInstant(readInstantNumber(source), ZoneOffset.UTC);
    }

    public OffsetTime readOffsetTime(final BinaryReader source) {
        return OffsetTime.parse(readRawString(source));
    }

    public OffsetTime readOffsetTimeNumber(final BinaryReader source) {
        return OffsetTime.of(readLocalTimeNumber(source), ZoneOffset.UTC);
    }

    public Period readPeriod(final BinaryReader source) {
        return Period.parse(readRawString(source));
    }

    public short readShort(final BinaryReader source) {
        return Short.parseShort(requireNotHex(readRawString(source)));
    }

    public String readString(final BinaryReader source) throws ReadException {
        source.readOffset(begin);

        final var buffer = new byte[length()];
        var b0 = 0; // Index of first unwritten byte in buffer.
        var p0 = begin; // Index of first non-appended byte in source.
        var badEscapeBuilder = new StringBuilder(0);
        error:
        {
            while (source.readOffset() < end) {
                var b = source.readByte();
                if (b == '\\') {
                    // Collect bytes before escape sequence into buffer.
                    {
                        final var length = source.readOffset() - p0;
                        source.readOffset(p0);
                        source.readBytes(buffer, b0, length);
                        b0 += length;
                        p0 = source.readOffset();
                    }

                    if (p0 == end) {
                        badEscapeBuilder.append('\\');
                        break error;
                    }

                    b = source.readByte();
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
                        if (source.readOffset() + 4 <= end) {
                            try {
                                source.readBytes(uBuffer);
                                if (uBuffer[0] != '+') {
                                    final var uString = new String(uBuffer, StandardCharsets.ISO_8859_1);
                                    final var uNumber = Integer.parseUnsignedInt(uString, 16);
                                    final var uBytes = Character.toString(uNumber)
                                        .getBytes(StandardCharsets.ISO_8859_1);
                                    for (byte uByte : uBytes) {
                                        buffer[b0++] = uByte;
                                    }
                                    continue;
                                }
                            }
                            catch (final NumberFormatException ignored) {}
                        }
                        else {
                            source.readBytes(uBuffer, 0, end - source.readOffset());
                        }
                        badEscapeBuilder.append("\\u").append(new String(uBuffer, StandardCharsets.US_ASCII));
                        break error;

                    default:
                        badEscapeBuilder.append('\\').append(Character.toString(b));
                        break error;
                    }
                    buffer[b0++] = b;
                }
            }
            final var length = source.readOffset() - p0;
            source.readOffset(p0);
            source.readBytes(buffer, b0, length);
            return new String(buffer, StandardCharsets.UTF_8);
        }
        throw new ReadException(DataEncoding.JSON, "Bad escape", badEscapeBuilder.toString(), p0);
    }

    public Year readYear(final BinaryReader source) {
        return Year.parse(readRawString(source));
    }

    public Year readYearNumber(final BinaryReader source) {
        final var number = Double.parseDouble(readRawString(source));
        return Year.of((int) number);
    }

    public YearMonth readYearMonth(final BinaryReader source) {
        return YearMonth.parse(readRawString(source));
    }

    public ZonedDateTime readZonedDateTimeNumber(final BinaryReader source) {
        return ZonedDateTime.ofInstant(readInstantNumber(source), ZoneOffset.UTC);
    }

    public ZoneId readZoneId(final BinaryReader source) {
        return ZoneId.of(readRawString(source));
    }

    public ZoneOffset readZoneOffset(final BinaryReader source) {
        return ZoneOffset.of(readRawString(source));
    }

    public ZoneOffset readZoneOffsetNumber(final BinaryReader source) {
        final var number = Double.parseDouble(readRawString(source));
        return ZoneOffset.ofTotalSeconds((int) number);
    }

    private String readRawString(final BinaryReader source) {
        final var buffer = new byte[length()];
        source.getBytes(begin, buffer);
        return new String(buffer, StandardCharsets.ISO_8859_1);
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
