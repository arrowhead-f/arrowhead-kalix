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
        error:
        {
            while (source.position() < end) {
                var b = source.get();
                if (b == '\\') {
                    final var bl = source.position() - p0;
                    source.get(buffer, b0, bl);
                    b0 += bl;
                    p0 = source.position();

                    if (source.position() == end) {
                        break error;
                    }

                    b = source.get();
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
                        if (source.position() + 4 >= end) {
                            break error;
                        }
                        try {
                            final var ubuf = new byte[4];
                            source.get(ubuf);
                            if (ubuf[0] == '+') {
                                break error;
                            }
                            final var ustr = new String(ubuf, StandardCharsets.ISO_8859_1);
                            final var unum = Character.toString(Integer.parseUnsignedInt(ustr, 16));
                            final var ubytes = unum.getBytes(StandardCharsets.ISO_8859_1);
                            for (byte ub : ubytes) {
                                buffer[b0++] = ub;
                            }
                            continue;
                        }
                        catch (final NumberFormatException ignored) {}
                        break error;

                    default:
                        break error;
                    }
                    buffer[b0++] = b;
                }
            }
            source.get(buffer, b0, source.position() - p0);
            return new String(buffer, StandardCharsets.UTF_8);
        }
        throw new JsonSyntaxException(
            "Bad escape",
            "TODO", //new String(source, begin, end - begin, StandardCharsets.UTF_8),
            p0
        );
    }
}
