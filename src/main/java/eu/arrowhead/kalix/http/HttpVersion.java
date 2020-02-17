package eu.arrowhead.kalix.http;

import java.util.Objects;

/**
 * Denotes HTTP protocol version.
 *
 * @see <a href="https://tools.ietf.org/html/rfc1945">RFC 1945</a>
 * @see <a href="https://tools.ietf.org/html/rfc7230">RFC 7230</a>
 * @see <a href="https://tools.ietf.org/html/rfc7231">RFC 7231</a>
 * @see <a href="https://tools.ietf.org/html/rfc7232">RFC 7232</a>
 * @see <a href="https://tools.ietf.org/html/rfc7233">RFC 7233</a>
 * @see <a href="https://tools.ietf.org/html/rfc7234">RFC 7234</a>
 * @see <a href="https://tools.ietf.org/html/rfc7235">RFC 7235</a>
 * @see <a href="https://tools.ietf.org/html/rfc7235">RFC 7540</a>
 */
public class HttpVersion {
    private final int major;
    private final int minor;
    private final String text;
    private boolean isStandard;

    private HttpVersion(final int major, final int minor, final String text, final boolean isStandard) {
        this.major = major;
        this.minor = minor;
        this.text = text;
        this.isStandard = isStandard;
    }

    /**
     * @return HTTP protocol major version.
     */
    public int major() {
        return major;
    }

    /**
     * @return HTTP protocol minor version.
     */
    public int minor() {
        return minor;
    }

    /**
     * @return HTTP version string, consisting of the major version, a dot and
     * the minor version.
     */
    private String text() {
        return text;
    }

    /**
     * @return Whether or not this version represents a standardized HTTP
     * protocol version.
     *
     * @see <a href="https://tools.ietf.org/html/rfc1945">RFC 1945</a>
     * @see <a href="https://tools.ietf.org/html/rfc7230">RFC 7230</a>
     * @see <a href="https://tools.ietf.org/html/rfc7231">RFC 7231</a>
     * @see <a href="https://tools.ietf.org/html/rfc7232">RFC 7232</a>
     * @see <a href="https://tools.ietf.org/html/rfc7233">RFC 7233</a>
     * @see <a href="https://tools.ietf.org/html/rfc7234">RFC 7234</a>
     * @see <a href="https://tools.ietf.org/html/rfc7235">RFC 7235</a>
     * @see <a href="https://tools.ietf.org/html/rfc7235">RFC 7540</a>
     */
    public boolean isStandard() {
        return isStandard;
    }

    /**
     * HTTP version 1.0.
     */
    public static final HttpVersion HTTP_10 = new HttpVersion(1, 0, "1.0", true);

    /**
     * HTTP version 1.1.
     */
    public static final HttpVersion HTTP_11 = new HttpVersion(1, 1, "1.1", true);

    /**
     * HTTP version 2.0.
     */
    public static final HttpVersion HTTP_20 = new HttpVersion(2, 0, "2.0", true);

    /**
     * HTTP version 3.0.
     */
    public static final HttpVersion HTTP_30 = new HttpVersion(3, 0, "3.0", false);

    /**
     * Parses given HTTP version string, expected to be on the form
     * {@code <major>.<minor>}.
     * <p>
     * If parsed version is a standardized such, a cached {@link HttpVersion}
     * is returned. Otherwise, a new instance is returned.
     *
     * @param majorDotMinor HTTP version to parse.
     * @return Existing or new {@link HttpVersion}.
     */
    public static HttpVersion valueOf(final String majorDotMinor) {
        switch (majorDotMinor) {
            case "1.0": return HTTP_10;
            case "1.1": return HTTP_11;
            case "2.0": return HTTP_20;
            case "3.0": return HTTP_30;
            default:
                break;
        }
        final var dotIndex = majorDotMinor.indexOf('.');
        if (dotIndex == -1 || dotIndex == majorDotMinor.length() - 1) {
            throw new IllegalArgumentException("Expected HTTP version to be on form `<major>.<minor>`.");
        }
        final var major = Integer.parseInt(majorDotMinor.substring(0, dotIndex));
        final var minor = Integer.parseInt(majorDotMinor.substring(dotIndex + 1));
        return new HttpVersion(major, minor, majorDotMinor, false);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        HttpVersion that = (HttpVersion) o;
        return major == that.major &&
            minor == that.minor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor);
    }

    @Override
    public String toString() {
        return text;
    }
}
