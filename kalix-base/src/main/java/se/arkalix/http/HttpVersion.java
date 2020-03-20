package se.arkalix.http;

import java.util.Objects;

/**
 * HTTP protocol version.
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
     * Either acquires a cached HTTP version object matching the given
     * major and minor versions or creates a new one.
     *
     * @param major Major HTTP version.
     * @param minor Minor HTTP version.
     * @return New or existing HTTP version object.
     */
    public static HttpVersion getOrCreate(final int major, final int minor) {
        switch (major) {
        case 1:
            switch (minor) {
            case 0: return HTTP_10;
            case 1: return HTTP_11;
            }
            break;
        case 2:
            if (minor == 0) {
                return HTTP_20;
            }
            break;
        case 3:
            if (minor == 0) {
                return HTTP_30;
            }
            break;
        }
        return new HttpVersion(major, minor, "HTTP/" + major + "." + minor, false);
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
     * @return HTTP version string, such as {@code "HTTP/1.1"}.
     */
    private String text() {
        return text;
    }

    /**
     * @return Whether or not this version represents a standardized HTTP
     * protocol version.
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
    public static final HttpVersion HTTP_10 = new HttpVersion(1, 0, "HTTP/1.0", true);

    /**
     * HTTP version 1.1.
     */
    public static final HttpVersion HTTP_11 = new HttpVersion(1, 1, "HTTP/1.1", true);

    /**
     * HTTP version 2.0.
     */
    public static final HttpVersion HTTP_20 = new HttpVersion(2, 0, "HTTP/2.0", true);

    /**
     * HTTP version 3.0.
     */
    public static final HttpVersion HTTP_30 = new HttpVersion(3, 0, "HTTP/3.0", false);

    /**
     * Resolves {@link HttpVersion} from given version string, expected to be
     * on the form {@code "HTTP/<major>.<minor>"}.
     * <p>
     * If parsed version is a standardized such, a cached {@link HttpVersion}
     * is returned. Otherwise, a new instance is returned.
     *
     * @param version Version to parse. Case sensitive, as required by RFC
     *                7230, Section 2.6.
     * @return Cached or new {@link HttpVersion}.
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-2.6">RFC 7230, Section 2.6</a>
     */
    public static HttpVersion valueOf(final String version) {
        error:
        {
            if (!version.startsWith("HTTP/")) {
                break error;
            }
            final var majorDotMinor = version.substring(5);
            switch (majorDotMinor) {
            case "1.0": return HTTP_10;
            case "1.1": return HTTP_11;
            case "2.0": return HTTP_20;
            case "3.0": return HTTP_30;
            }
            final var dotIndex = majorDotMinor.indexOf('.');
            if (dotIndex == -1 || dotIndex == majorDotMinor.length() - 1) {
                break error;
            }
            final var major = Integer.parseInt(majorDotMinor.substring(0, dotIndex));
            final var minor = Integer.parseInt(majorDotMinor.substring(dotIndex + 1));
            return new HttpVersion(major, minor, version, false);
        }
        throw new IllegalArgumentException("Invalid HTTP version `" + version +
            "`; expected `HTTP/<major>.<minor>`");
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
