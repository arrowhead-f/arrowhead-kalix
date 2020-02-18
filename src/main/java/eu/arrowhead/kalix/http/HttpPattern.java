package eu.arrowhead.kalix.http;

import java.util.*;

public class HttpPattern {
    private final String pattern;
    private final int nParameters;
    private final boolean isPrefix;

    private HttpPattern(final String pattern, final int nParameters, final boolean isPrefix) {
        this.pattern = pattern;
        this.nParameters = nParameters;
        this.isPrefix = isPrefix;
    }

    /**
     * The root pattern, only matching the "/" path.
     */
    public static final HttpPattern ROOT = new HttpPattern("/", 0, false);

    public boolean apply(final String path, final List<String> parameters) {
        // p0 = start of path;    p1 = end of path.
        // q0 = start of pattern; q1 = end of pattern.

        var p1 = path.length();
        if (p1 > 1 && path.charAt(p1 - 1) == '/') {
            p1 -= 1;
        }
        // We have already stripped any trailing '/' from the pattern.
        final var q1 = pattern.length();

        if (!isPrefix && nParameters == 0 && p1 != q1) {
            return false;
        }

        var hasFoundParameter = false;
        int p0 = 0, q0 = 0;
        while (true) {
            // Find first non-identical character.
            while (p0 < p1 && q0 < q1 && path.charAt(p0) == pattern.charAt(q0)) {
                p0 += 1;
                q0 += 1;
            }
            // Are we done?
            final var pAtEnd = p0 == p1;
            final var qAtEnd = q0 == q1;
            if (pAtEnd && qAtEnd || isPrefix && qAtEnd) {
                return true;
            }
            if (pAtEnd || qAtEnd) {
                return false;
            }
            // We must be at a path parameter. Collect segment from path.
            if (!hasFoundParameter) {
                parameters.clear();
                hasFoundParameter = true;
            }
            var px = p0;
            while (px < p1 && path.charAt(px) != '/') {
                px += 1;
            }
            parameters.add(path.substring(p0, px));
            p0 = px;
            q0 += 1;
        }
    }

    public static HttpPattern valueOf(final String pattern) {
        // q0 = start of pattern; q1 = end of pattern
        var q1 = pattern.length();
        if (q1 == 0 || pattern.charAt(0) != '/') {
            throw new IllegalArgumentException("Patterns must start with `/`.");
        }
        if (q1 == 1) {
            return ROOT;
        }
        if (pattern.charAt(q1 - 1) == '/') {
            q1 -= 1;
        }

        final var buffer = new StringBuilder(pattern.length());
        var nParameters = 0;
        var isPrefix = false;

        outer:
        for (var q0 = 0; q0 < q1; ++q0) {
            var cp = pattern.codePointAt(q0);
            if (cp == '%') {
                throw new IllegalArgumentException("Percent encodings may not be used in patterns.");
            }
            if (cp != '/') {
                buffer.append(requirePathCharacter(cp));
                continue;
            }
            segment:
            while (true) {
                buffer.append('/');
                if (q0 + 1 == q1) {
                    break outer;
                }
                cp = pattern.codePointAt(q0 + 1);
                switch (cp) {
                case '#':
                    buffer.append('#');
                    nParameters += 1;
                    q0 += 2;
                    while (q0 < q1) {
                        cp = pattern.codePointAt(q0);
                        if (cp == '/') {
                            continue segment;
                        }
                        q0 += 1;
                    }
                    break;

                case '.':
                    var isRelative = false;
                    if (q0 + 2 == q1) {
                        isRelative = true; // Pattern ends with "/."
                    }
                    else {
                        cp = pattern.codePointAt(q0 + 2);
                        if (cp == '.' && (q0 + 3 == q1 || pattern.codePointAt(q0 + 3) == '/')) {
                            isRelative = true; // Pattern ends with "/.." or contains "/../"
                        }
                        else if (cp == '/') {
                            isRelative = true; // Pattern contains "/./"
                        }
                    }
                    if (isRelative) {
                        throw new IllegalArgumentException("Relative paths may not be used in patterns.");
                    }

                case '>':
                    if (q0 + 2 == q1) {
                        isPrefix = true;
                        break outer;
                    }
                    throw new IllegalArgumentException("`>` may only occur at the very end of a pattern, right " +
                        "after the last `/`, to make the pattern allow subpaths.");
                }
                break;
            }
        }

        return new HttpPattern(buffer.toString(), nParameters, isPrefix);
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
    private static char requirePathCharacter(final int c) {
        final var isValid = c >= 0x61 && c <= 0x7A || c >= 0x24 && c <= 0x3B ||
            c >= 0x40 && c <= 0x5A || c == 0x5F || c == 0x7E || c == 0x3D ||
            c == 0x21;
        if (isValid) {
            return Character.toChars(c)[0];
        }
        throw new IllegalArgumentException(c == '#'
            ? "`#` may only occur right after a `/` to promote its segment to a path parameter."
            : "Invalid pattern character `" + Character.toString(c) + "`; "
            + "expected one of `0–9 A–Z a–z - . _ ~ % / ! $ & ' ( ) * + , ; = "
            + ": @`.");
    }

    @Override
    public String toString() {
        return pattern;
    }
}
