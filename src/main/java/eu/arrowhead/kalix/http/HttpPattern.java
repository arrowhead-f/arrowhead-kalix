package eu.arrowhead.kalix.http;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpPattern {
    private final byte[] pattern;
    private final int parameterCount;

    private HttpPattern(final byte[] pattern, final int parameterCount) {
        this.pattern = pattern;
        this.parameterCount = parameterCount;
    }

    /**
     * The root pattern, only matching the "/" path.
     */
    public static final HttpPattern ROOT = new HttpPattern(new byte[0], 0);

    public Optional<List<String>> apply(final String path) {
        final var parameters = new ArrayList<String>(parameterCount);
        if (this == ROOT) {
            return Optional.of(parameters);
        }
        final var pathBytes = path.getBytes(StandardCharsets.ISO_8859_1);

        int patternOffset = 0;
        int pathOffset = 0;
        while (true) {
            final var m = Arrays.mismatch(
                pattern, patternOffset, pattern.length,
                pathBytes, pathOffset, pathBytes.length);
            if (m == -1) {
                break;
            }
            patternOffset += m;
            if (patternOffset == pattern.length) {
                if (pathBytes.length == patternOffset + 1 && pathBytes[patternOffset] == '/') {
                    break;
                }
                else {
                    return Optional.empty();
                }
            }
            final var q = pattern[patternOffset];
            if (q == '#') {
                pathOffset += m;
                var i = pathOffset;
                while (i < pathBytes.length && pathBytes[i] != '/') {
                    i += 1;
                }
                parameters.add(new String(Arrays.copyOfRange(pathBytes, pathOffset, i), StandardCharsets.ISO_8859_1));
                if (i == pathBytes.length) {
                    break;
                }
                else {
                    pathOffset = i;
                    continue;
                }
            }
            if (q == '>') {
                break;
            }
            return Optional.empty();
        }
        return Optional.of(parameters);
    }

    public static HttpPattern valueOf(final String pattern) {
        var p1 = pattern.length();
        if (p1 == 0 || pattern.charAt(0) != '/') {
            throw new IllegalArgumentException("Patterns must start with `/`.");
        }
        if (p1 == 1) {
            return ROOT;
        }
        if (pattern.charAt(p1 - 1) == '/') {
            p1 -= 1;
        }

        final var compressed = new byte[p1];
        int c1 = 0; // Current length of compressed pattern
        var parameterCount = 0;

        outer:
        for (var p0 = 0; p0 < p1; ++p0) {
            var cp = pattern.codePointAt(p0);
            if (cp == '%') {
                throw new IllegalArgumentException("Percent encodings may not "
                    + "be used in patterns.");
            }
            segment:
            while (cp == '/') {
                if (p0 + 1 == p1) {
                    break outer;
                }
                cp = pattern.codePointAt(p0 + 1);
                switch (cp) {
                case '#':
                    compressed[c1++] = '/';
                    compressed[c1++] = '#';
                    parameterCount += 1;
                    p0 += 2;
                    while (p0 < p1) {
                        cp = pattern.codePointAt(p0);
                        if (cp == '/') {
                            continue segment;
                        }
                        p0 += 1;
                    }
                    break;

                case '.':
                    var isRelative = false;
                    if (p0 + 2 == p1) {
                        isRelative = true; // Pattern ends with "/."
                    }
                    else {
                        cp = pattern.codePointAt(p0 + 2);
                        switch (cp) {
                        case '.':
                            if (p0 + 3 == p1 || pattern.codePointAt(p0 + 3) == '/') {
                                // Pattern ends with "/.." or contains "/../"
                                isRelative = true;
                            }
                            break;

                        case '/':
                            isRelative = true; // Pattern contains "/./"
                            break;
                        }
                    }
                    if (isRelative) {
                        throw new IllegalArgumentException("Relative paths may "
                            + "not be used in patterns.");
                    }
                    break;

                case '>':
                    if (p0 + 2 == p1) {
                        compressed[c1++] = '/';
                        compressed[c1++] = '>';
                        break outer;
                    }
                    throw new IllegalArgumentException("`>` may only occur "
                        + "right after the last `/` of a pattern to signify "
                        + "that any subpath is to be considered a match.");
                }
                break;
            }
            compressed[c1++] = requirePathCharacter(cp);
        }

        return new HttpPattern(Arrays.copyOf(compressed, c1), parameterCount);
    }

    /**
     * Requires that given code point represents a valid URI path character, or
     * throws an exception. The following are the valid path characters:
     * <pre>
     * A–Z a–z 0–9 - . _ ~ % / ! $ & ' ( ) * + , ; = : @
     * </pre>
     *
     * @param c Tested code point.
     * @return {@code c} cast to {@code byte}.
     * @throws IllegalArgumentException If {@code c} is not a valid path
     *                                  character.
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986, Section 3.3</a>
     */
    private static byte requirePathCharacter(final int c) {
        final var isValid = c >= 0x61 && c <= 0x7A || c >= 0x24 && c <= 0x3B ||
            c >= 0x40 && c <= 0x5A || c == 0x5F || c == 0x7E || c == 0x3D ||
            c == 0x21;
        if (isValid) {
            return (byte) c;
        }
        throw new IllegalArgumentException(c == 0x23
            ? "`#` may only occur right after a `/` to promote its segment to "
            + "a path parameter."
            : "Invalid pattern character `" + Character.toString(c) + "`; "
            + "expected one of `0–9 A–Z a–z - . _ ~ % / ! $ & ' ( ) * + , ; = "
            + ": @`.");
    }
}
