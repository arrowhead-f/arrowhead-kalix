package se.arkalix.net.http.service;

import se.arkalix.net.Uris;

import java.util.*;

/**
 * A pattern that matches a well-defined set of paths.
 * <p>
 * At its simplest, a pattern is a regular URI path, as defined by RFC 3986,
 * Section 3.3. Such a path starts with a forward slash ({@code /}) and then
 * continues with zero or more segments separated by forward slashes. An
 * optional forward slash may be located at the end. While it is recommended
 * for segments to contain only the alphanumeric ASCII characters and hyphens
 * to maximize compatibility with various HTTP libraries and frameworks, RFC
 * 3986 explicitly allows the following characters, as well as so-called
 * <i>percent codecs</i> to be used:
 * <pre>
 * A–Z a–z 0–9 - . _ ~ ! $ &amp; ' ( ) * + , ; = : @
 * </pre>
 * While all these characters are allowed in segments provided to this pattern
 * implementation, percent codecs are not (e.g. {@code %20} as a
 * representation for ASCII space).
 * <p>
 * It is frequently useful to allow certain pattern segments to match any
 * segment at their positions in provided paths. For this reason, pattern
 * segments may be qualified as <i>path parameters</i> by adding a hash
 * ({@code #}) at the beginning of the segment (e.g. {@code /some/#parameter}
 * or {@code /some/other/#parameter/path}). When a path is successfully matched
 * against a pattern, any path parameter segments are collected from the path
 * into a path parameter list.
 * <p>
 * A pattern may optionally end with a right angle bracket ({@code >}) to
 * denote that the pattern is to be considered a prefix. Prefix patterns match
 * all paths with matching segments up until the right angle bracket. When such
 * a prefix pattern is matched successfully against a path, the segments after
 * the prefix is collected into any path parameter list.
 *
 * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986, Section 3.3</a>
 */
public class HttpPattern implements Comparable<HttpPattern> {
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

    /**
     * Matches this pattern against given path, and collects any path
     * parameters into the provided list.
     * <p>
     * Note that {@link List#clear()} will be called on the list if any
     * parameter is identified by the pattern in {@code path}. Furthermore,
     * even if this method returns {@code false}, it may have collected
     * parameters into the provided list before returning.
     * <p>
     * If this pattern contains path parameters or is a prefix, any segments
     * matching a path parameter or occurs after the prefix are <b>not</b>
     * validated, and may, therefore, contain invalid path characters, as
     * dictated by RFC 3986.
     *
     * @param path       Path to match against.
     * @param offset     Offset, from beginning of path, from which to start
     *                   matching.
     * @param parameters List to store any pattern path parameters in.
     * @return {@code true} only if {@code path} matches this pattern.
     * @see HttpPattern
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986, Section 3.3</a>
     */
    public boolean match(final String path, final int offset, final List<String> parameters) {
        // p0 = start of path;    p1 = end of path.
        // q0 = start of pattern; q1 = end of pattern.

        var p1 = path.length();
        if (p1 > 1 && path.charAt(p1 - 1) == '/') {
            p1 -= 1;
        }
        // We have already stripped any trailing '/' from the pattern.
        final var q1 = pattern.length();

        if (nParameters == 0) { // Fast path for patterns without parameters.
            final var px = p1 - offset;

            if (px == 0) { // If px is zero, match pattern if it is "/".
                return q1 == 1;
            }

            if (isPrefix && px > q1 && pattern.regionMatches(0, path, offset, q1)) {
                parameters.add(path.substring(q1 - 1));
                return true;
            }
            return px == q1 && pattern.regionMatches(0, path, offset, q1);
        }

        int np = 0; // Number of found path parameters.
        int p0 = offset, q0 = 0;
        while (true) {
            // Find first non-identical character.
            while (p0 < p1 && q0 < q1 && path.charAt(p0) == pattern.charAt(q0)) {
                p0 += 1;
                q0 += 1;
            }
            // Are we done?
            final var pAtEnd = p0 == p1;
            final var qAtEnd = q0 == q1;
            if (qAtEnd) {
                if (pAtEnd) {
                    return np == nParameters;
                }
                if (isPrefix) {
                    parameters.add(path.substring(p0 - 1));
                    return np == nParameters;
                }
                return false;
            }
            if (pAtEnd) {
                return false;
            }

            // We must be at a path parameter. Collect segment from path.
            int px = p0;
            while (px < p1 && path.charAt(px) != '/') {
                px += 1;
            }

            if (np == 0) {
                parameters.clear();
            }
            np += 1;
            parameters.add(path.substring(p0, px));

            p0 = px;
            q0 += 1; // Skip '#'.
        }
    }

    /**
     * Determines whether this pattern matches at least some paths that are
     * also matched by the provided other pattern.
     *
     * @param other Pattern to compare to.
     * @return {@code true} only if this pattern matches some of the paths
     * that the other pattern does.
     */
    public boolean intersectsWith(final HttpPattern other) {
        // q  = this pattern.
        // q0 = start of this pattern;  q1 = end of this pattern.

        // r = other pattern.
        // r0 = start of other pattern; r1 = end of other pattern.

        final var q = pattern;
        var q0 = 0;
        final var q1 = q.length();

        final var r = other.pattern;
        var r0 = 0;
        final var r1 = other.pattern.length();

        while (true) {
            // Find first non-identical character.
            while (q0 < q1 && r0 < r1 && q.charAt(q0) == r.charAt(r0)) {
                q0 += 1;
                r0 += 1;
            }

            // Are we done?
            final var qAtEnd = q0 == q1;
            final var rAtEnd = r0 == r1;
            if (rAtEnd) {
                return qAtEnd || other.isPrefix;
            }
            if (qAtEnd) {
                return isPrefix;
            }
            if (q.charAt(q0) != '#' && r.charAt(r0) != '#') {
                return false;
            }

            // One pattern is at a path parameter. Skip current segment.
            while (q0 < q1 && q.charAt(q0) != '/') {
                q0 += 1;
            }
            while (r0 < r1 && r.charAt(r0) != '/') {
                r0 += 1;
            }
        }
    }

    /**
     * @return Number of path parameters.
     */
    public int nParameters() {
        return nParameters;
    }

    /**
     * @return Text representation of this pattern.
     */
    public String text() {
        return pattern + (isPrefix ? ">" : "");
    }

    /**
     * Produces {@link HttpPattern} from given pattern string.
     * <p>
     * Valid patterns must start with a forward slash and consist of zero or
     * more segments separated by forward slashes. A segment must not contain
     * percent codecs, but may otherwise consist of any valid path segment
     * character, as defined by RFC 3986. A segment may optionally start with
     * a hash ({@code #}) to make it into a path parameter. The last segment
     * may consist only of a right angle bracket ({@code >}) to make the
     * pattern into a so-called <i>prefix-pattern</i>.
     *
     * @param pattern String to parse.
     * @return New {@link HttpPattern} or {@link #ROOT} if {@code pattern} is {@code "/"}.
     * @throws IllegalArgumentException If pattern is invalid.
     * @see HttpPattern
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986, Section 3.3</a>
     */
    public static HttpPattern valueOf(final String pattern) {
        // q0 = start of pattern; q1 = end of pattern
        var q1 = pattern.length();
        if (q1 == 0 || pattern.charAt(0) != '/') {
            throw new IllegalArgumentException("Patterns must start with `/`");
        }
        if (q1 == 1) {
            return ROOT;
        }
        if (pattern.charAt(q1 - 1) == '/') {
            q1 -= 1;
        }

        final var builder = new StringBuilder(pattern.length());
        var nParameters = 0;
        var isPrefix = false;

        outer:
        for (var q0 = 0; q0 < q1; ++q0) {
            var c = pattern.charAt(q0);
            if (c == '%') {
                throw new IllegalArgumentException("Percent codecs may not be used in patterns");
            }
            if (c != '/') {
                if (Uris.isValidPathCharacter(c)) {
                    builder.append(c);
                    continue;
                }
                throw new IllegalArgumentException(c == '#'
                    ? "`#` may only occur right after a `/` to promote its segment to a path parameter."
                    : "Invalid pattern character `" + c + "`; expected one of `0–9 A–Z a–z -._~!$&'()*+,;=:@`");
            }
            segment:
            while (true) {
                builder.append('/');
                if (q0 + 1 == q1) {
                    break outer;
                }
                c = pattern.charAt(q0 + 1);
                switch (c) {
                case '#': // Path parameter segment. Omit parameter name from pattern.
                    builder.append('#');
                    nParameters += 1;
                    q0 += 2;
                    while (q0 < q1) {
                        c = pattern.charAt(q0);
                        if (c == '/') {
                            continue segment;
                        }
                        q0 += 1;
                    }
                    break;

                case '.': // Ensure segment is not a relative path directive.
                    var isRelative = false;
                    if (q0 + 2 == q1) {
                        isRelative = true; // Pattern ends with "/.".
                    }
                    else {
                        c = pattern.charAt(q0 + 2);
                        if (c == '.' && (q0 + 3 == q1 || pattern.charAt(q0 + 3) == '/')) {
                            isRelative = true; // Pattern ends with "/.." or contains "/../".
                        }
                        else if (c == '/') {
                            isRelative = true; // Pattern contains "/./".
                        }
                    }
                    if (isRelative) {
                        throw new IllegalArgumentException("Relative paths may not be used in patterns");
                    }

                case '>': // Makes pattern into a prefix. Ensure it is the last thing in the pattern.
                    if (q0 + 2 == pattern.length()) {
                        isPrefix = true;
                        break outer;
                    }
                    throw new IllegalArgumentException("`>` may only occur at the very end of a pattern, right " +
                        "after the last `/`, to make the pattern allow any subpaths");
                }
                break;
            }
        }

        return new HttpPattern(builder.toString(), nParameters, isPrefix);
    }

    /**
     * <i>The purpose of this comparator, which may seem rather arbitrary, is
     * to ensure that pattern lists sorted with it are ordered such that
     * comparing a path to each pattern in order leads to the most specialized
     * pattern first matching the path. A pattern is more specialized than
     * another such only if the subset of paths it can match is smaller than
     * that of another pattern.</i>
     * <p>
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final HttpPattern other) {
        final var limit = Math.min(pattern.length(), other.pattern.length());
        for (var i = 0; i < limit; i++) {
            final var c0 = pattern.charAt(i);
            final var c1 = other.pattern.charAt(i);
            if (c0 != c1) {
                // Paths with parameters come after those without parameters.
                if (c0 == '#') {
                    return 1;
                }
                if (c1 == '#') {
                    return -1;
                }

                // Prefix paths come after both parameterized and regular paths.
                final var b = Boolean.compare(isPrefix, other.isPrefix);
                if (b != 0) {
                    return b;
                }

                return c0 - c1;
            }
        }
        // Longer paths before shorter paths.
        return other.pattern.length() - pattern.length();
    }

    @Override
    public String toString() {
        return text();
    }
}
