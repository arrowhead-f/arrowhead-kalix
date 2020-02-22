package eu.arrowhead.kalix.util.io.json;

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

    double intoNumber(final byte[] source) throws JsonSyntaxError {
        final var string = new String(source, begin, end - begin, StandardCharsets.ISO_8859_1);
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
        throw new JsonSyntaxError("Bad number", string, begin);
    }

    String intoString(final byte[] source) throws JsonSyntaxError {
        final var builder = new StringBuilder(end - begin);
        // i0 = index of first non-appended byte; i1 = current byte
        var i0 = begin;
        var i1 = begin;
        error:
        {
            for (; i1 < end; ++i1) {
                var b = source[i1];
                char c;
                if (b == '\\') {
                    if (i0 < i1) {
                        builder.append(new String(source, i0, i1 - i0, StandardCharsets.UTF_8));
                        i0 = i1;
                    }
                    // We have already ensured in JsonTokenizer that no
                    // backward slashes are followed by the end of the string.
                    b = source[++i1];
                    switch (b) {
                    case '\"':
                    case '/':
                    case '\\':
                        c = (char) b;
                        break;

                    case 'b': c = '\b'; break;
                    case 'f': c = '\f'; break;
                    case 'r': c = '\r'; break;
                    case 'n': c = '\n'; break;
                    case 't': c = '\t'; break;

                    case 'u':
                        // We have already ensured in JsonTokenizer that all
                        // unicode escapes are followed by at least four bytes
                        // before the string ends.
                        i1 += 1;
                        try {
                            if (source[i1] != '+') {
                                final var number = new String(source, i1, end - i1, StandardCharsets.ISO_8859_1);
                                i1 += 4;
                                builder.append(Character.toString(Integer.parseUnsignedInt(number, 16)));
                                continue;
                            }
                        }
                        catch (final NumberFormatException ignored) {}
                        break error;

                    default:
                        break error;
                    }
                    builder.append(c);
                }
            }
            if (i0 < i1) {
                builder.append(new String(source, i0, i1 - i0, StandardCharsets.UTF_8));
            }
            return builder.toString();
        }
        throw new JsonSyntaxError(
            "Bad escape",
            new String(source, begin, end - begin, StandardCharsets.UTF_8),
            i1
        );
    }
}
