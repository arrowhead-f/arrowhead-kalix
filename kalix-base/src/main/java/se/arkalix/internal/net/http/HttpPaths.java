package se.arkalix.internal.net.http;

import se.arkalix.util.annotation.Internal;

/**
 * Various helper functions for managing URL paths.
 */
@Internal
public class HttpPaths {
    private HttpPaths() {}

    /**
     * Tests whether given string {@code path} is a valid RFC 3986 path.
     * <p>
     * Concretely, this means that {@code path} must begin with a forward slash
     * ({@code /}) and then consist of only the following characters:
     * <pre>
     *     A–Z a–z 0–9 - . _ ~ ! $ &amp; ' ( ) * + , ; / = : @
     * </pre>
     * As an exception, strings are allowed to contain arbitrary percent
     * encodings on the form {@code %<hex-digit><hex-digit>} (e.g. {@code %20}).
     * <p>
     * No checks are made to determine whether any percent encodings actually
     * conform to any character set, ASCII or otherwise. This might change in
     * the future, given that a strong enough cause for it can be presented.
     *
     * @param path Tested path.
     * @return {@code true} only if {@code path} is valid.
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986, Section 3.3</a>
     */
    public static boolean isValidPath(final String path) {
        final int p1 = path.length();
        if (p1 == 0 || path.charAt(0) != '/') {
            return false;
        }
        for (int p0 = 1; p0 < p1; ++p0) {
            final var ch = path.charAt(p0);
            if (ch == '%') {
                if (((long) p0 + 2) >= (long) p1) {
                    return false;
                }
                if (isHex(path.charAt(++p0)) && isHex(path.charAt(++p0))) {
                    continue;
                }
            }
            else if (isValidPathCharacter(ch)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static boolean isHex(final char ch) {
        return ch >= '0' && ch <= '9' || ch >= 'A' && ch <= 'F' || ch >= 'a' && ch <= 'f';
    }

    /**
     * Tests whether given string {@code path} is a valid RFC 3986 path, as
     * well as not including any percent encodings.
     * <p>
     * Concretely, this means that {@code path} must begin with a forward slash
     * ({@code /}) and then consist of only the following characters:
     * <pre>
     *     A–Z a–z 0–9 - . _ ~ ! $ &amp; ' ( ) * + , ; / = : @
     * </pre>
     *
     * @param path Tested path.
     * @return {@code true} only if {@code path} is valid.
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986, Section 3.3</a>
     */
    public static boolean isValidPathWithoutPercentEncodings(final String path) {
        final var p1 = path.length();
        if (p1 == 0 || path.charAt(0) != '/') {
            return false;
        }
        for (var p0 = 1; p0 < p1; ++p0) {
            final var c = path.charAt(p0);
            if (c == '%' || !isValidPathCharacter(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests whether given character {@code c} is any valid RFC 3986 path
     * character.
     * <p>
     * Concretely, those characters are:
     * <pre>
     *     A–Z a–z 0–9 - . _ ~ ! $ % &amp; ' ( ) * + , ; / = : @
     * </pre>
     *
     * @param c Tested character.
     * @return {@code true} only if {@code c} is a valid path character.
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986, Section 3.3</a>
     */
    public static boolean isValidPathCharacter(final char c) {
        return c >= 'a' && c <= 'z' || c >= '$' && c <= ';' || c >= '@' && c <= 'Z' ||
            c == '_' || c == '~' || c == '=' || c == '!';
    }
}
