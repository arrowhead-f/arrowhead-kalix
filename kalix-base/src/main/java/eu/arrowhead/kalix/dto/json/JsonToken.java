package eu.arrowhead.kalix.dto.json;

import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.dto.ReadException;
import eu.arrowhead.kalix.dto.binary.BinaryReader;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

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

    public float readFloat(final BinaryReader source) {
        return Float.parseFloat(requireNotHex(readRawString(source)));
    }

    public int readInteger(final BinaryReader source) {
        return Integer.parseInt(requireNotHex(readRawString(source)));
    }

    public long readLong(final BinaryReader source) {
        return Long.parseLong(requireNotHex(readRawString(source)));
    }

    public short readShort(final BinaryReader source) {
        return Short.parseShort(requireNotHex(readRawString(source)));
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
}
