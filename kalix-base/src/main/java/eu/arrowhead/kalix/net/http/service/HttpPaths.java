package eu.arrowhead.kalix.net.http.service;

/**
 * Various helper functions for managing URL paths.
 */
class HttpPaths {
    private HttpPaths() {}

    /**
     * Tests whether given string {@code path} is a valid RFC 3986 path, as
     * well as not including any percent encodings.
     * <p>
     * Concretely, this means that {@code path} must begin with a forward slash
     * ({@code /}) and then consist of only the following characters:
     * <pre>
     *     A–Z a–z 0–9 - . _ ~ ! $ & ' ( ) * + , ; / = : @
     * </pre>
     *
     * @param path Tested path.
     * @return {@code true} only if {@code path} is valid.
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986, Section 3.3</a>
     */
    static boolean isValidPathWithoutPercentEncodings(final String path) {
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
     *     A–Z a–z 0–9 - . _ ~ ! $ % & ' ( ) * + , ; / = : @
     * </pre>
     *
     * @param c Tested character.
     * @return {@code true} only if {@code c} is a valid path character.
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986, Section 3.3</a>
     */
    static boolean isValidPathCharacter(final char c) {
        return c >= 'a' && c <= 'z' || c >= '$' && c <= ';' || c >= '@' && c <= 'Z' ||
            c == '_' || c == '~' || c == '=' || c == '!';
    }
}
