package eu.arrowhead.kalix.util.io.json;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

final class JsonToken {
    final JsonTokenType type;

    int begin, end;
    int nChildren;

    JsonToken(final JsonTokenType type, final int begin, final int end, final int nChildren) {
        this.type = type;
        this.begin = begin;
        this.end = end;
        this.nChildren = nChildren;
    }

    double intoNumber(final ByteBuffer source) throws JsonSyntaxException {
        final var buffer = new byte[end - begin];
        source.position(begin).get(buffer);
        final var string = new String(buffer, StandardCharsets.ISO_8859_1);
        error:
        try {
            if (string.length() > 2 && string.charAt(0) == '0') {
                final var x = string.charAt(1);
                if (x == 'x' || x == 'X') {
                    break error;
                }
            }
            return Double.parseDouble(string);
        }
        catch (final NumberFormatException ignored) {}
        throw new JsonSyntaxException("Bad number", string, begin);
    }

    String intoString(final ByteBuffer source) throws JsonSyntaxException {
        source.position(begin);

        final var buffer = new byte[end - begin];
        var b0 = 0; // Index of first unwritten byte in buffer.
        var p0 = begin; // Index of first non-appended byte in source.
        var badEscapeBuilder = new StringBuilder(0);
        error:
        {
            while (source.position() < end) {
                var b = source.get();
                if (b == '\\') {
                    // Collect bytes before escape sequence into buffer.
                    {
                        final var length = source.position() - p0;
                        source.get(buffer, b0, length);
                        b0 += length;
                        p0 = source.position();
                    }

                    if (p0 == end) {
                        badEscapeBuilder.append('\\');
                        break error;
                    }

                    b = source.get();
                    switch (b) {
                    case '\"':
                    case '/':
                    case '\\':
                        break;

                    case 'b': b = '\b';
                        break;
                    case 'f': b = '\f';
                        break;
                    case 'r': b = '\r';
                        break;
                    case 'n': b = '\n';
                        break;
                    case 't': b = '\t';
                        break;

                    case 'u':
                        final var uBuffer = new byte[4];
                        if (source.position() + 4 <= end) {
                            try {
                                source.get(uBuffer);
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
                            source.get(uBuffer, 0, end - source.position());
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
            source.get(buffer, b0, source.position() - p0);
            return new String(buffer, StandardCharsets.UTF_8);
        }
        throw new JsonSyntaxException("Bad escape", badEscapeBuilder.toString(), p0);
    }
}
