package eu.arrowhead.kalix;

import java.util.Objects;

/**
 * An Arrowhead interface triple, used to describe what application-level
 * transport interface, security interface and message encoding a given
 * {@link ArrowheadService} uses.
 */
public class Triple {
    /**
     * Represents a {@link ArrowheadService} that communicates over HTTPS with
     * message payloads encoded with JSON.
     */
    public static final Triple HTTP_SECURE_JSON = new Triple("HTTP", true, "JSON");

    /**
     * Represents a {@link ArrowheadService} that communicates over plain HTTP
     * with message payloads encoded with JSON.
     */
    public static final Triple HTTP_INSECURE_JSON = new Triple("HTTP", false, "JSON");

    private final String transport;
    private final boolean isSecure;
    private final String encoding;

    /**
     * Creates new interface triple.
     * <p>
     * Note that {@code "HTTPS"} is *not* a valid transport interface
     * identifier. In that case, use {@code "HTTP"} as transport interface and
     * set {@code isSecure} to {@code true}.
     *
     * @param transport Transport interface identifier, such as {@code "COAP"}
     *                  or {@code "HTTP"}.
     * @param isSecure  Whether or not transport security is enabled.
     * @param encoding  Message payload format, such as {@code "CBOR"} or
     *                  {@code "JSON"}.
     */
    public Triple(final String transport, final boolean isSecure, final String encoding) {
        this.transport = Objects.requireNonNull(transport).toUpperCase();
        this.isSecure = isSecure;
        this.encoding = Objects.requireNonNull(encoding).toUpperCase();
    }

    /**
     * Parses given string into interface triple.
     * <p>
     * Valid strings should strictly adhere to the following regular expression:
     * <pre>
     * ([A-Z][0-9A-Z_]*)-(SECURE|INSECURE)-([A-Z][0-9A-Z_]*)
     * </pre>
     * However, this parser also ignores character case, accepts the names
     * {@code TLS} and {@code RAW} as alternatives to {@code SECURE} and
     * {@code INSECURE}, as well as accepting strings matching the following
     * special form:
     * <pre>
     * HTTPS-([A-Z][0-9A-Z_]*)
     * </pre>
     *
     * @param string String to parse.
     * @return Protocol triple.
     * @throws IllegalArgumentException If
     */
    public static Triple parse(final String string) {
        final var parts = string.split("-", 3);
        if (parts.length == 2 && parts[0].equalsIgnoreCase("HTTPS")) {
            if (isNameInvalid(parts[1])) {
                throw new IllegalArgumentException("Invalid Arrowhead interface string `" + string
                    + "`; should match `([A-Z][0-9A-Z_]*)-(SECURE|INSECURE)-([A-Z][0-9A-Z_]*)`");
            }
            return new Triple("HTTP", true, parts[1]);
        }
        if (parts.length != 3 || isNameInvalid(parts[0]) || isNameInvalid(parts[2])) {
            throw new IllegalArgumentException("Invalid Arrowhead interface string `" + string
                + "`; should match `([A-Z][0-9A-Z_]*)-(SECURE|INSECURE)-([A-Z][0-9A-Z_]*)`");
        }
        boolean isSecure;
        switch (parts[1].toUpperCase()) {
            case "SECURE":
            case "TLS":
                isSecure = true;
                break;

            case "INSECURE":
            case "RAW":
                isSecure = false;
                break;

            default:
                throw new IllegalArgumentException("Invalid Arrowhead security interface `"
                    + parts[1] + "`; use `SECURE` or `INSECURE`");
        }
        return new Triple(parts[0], isSecure, parts[2]);
    }

    private static boolean isNameInvalid(final String name) {
        final var bytes = name.getBytes();
        var i = 0;
        var b = bytes[0];

        // [A-Za-Z]
        if (!(b >= 0x41 && b <= 0x5A || b >= 0x61 && b <= 0x7A)) {
            return true;
        }
        i += 1;

        // [0-9A-Za-Z_]*
        for (; i < bytes.length; ++i) {
            b = bytes[i];
            if (!(b >= 0x30 && b <= 0x39 || b >= 0x41 && b <= 0x5A
                || b == 0x5F || b >= 0x61 && b <= 0x7A)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return Transport interface identifier, such as {@code "HTTP"} or
     * {@code "MQTT}.
     */
    public String getTransport() {
        return transport;
    }

    /**
     * @return Whether or not transport security is enabled.
     */
    public boolean isSecure() {
        return isSecure;
    }

    /**
     * @return Encoding interface identifier, such as {@code "JSON"} or
     * {@code "XML"}.
     */
    public String getEncoding() {
        return encoding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triple triple = (Triple) o;
        return transport.equals(triple.transport) &&
            isSecure == triple.isSecure &&
            encoding.equals(triple.encoding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transport, isSecure, encoding);
    }

    @Override
    public String toString() {
        return transport + (isSecure ? "SECURE" : "INSECURE") + encoding;
    }
}
