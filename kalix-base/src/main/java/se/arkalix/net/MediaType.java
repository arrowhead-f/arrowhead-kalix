package se.arkalix.net;

import se.arkalix.encoding.Encoding;
import se.arkalix.encoding.ToEncoding;
import se.arkalix.util.annotation.ThreadSafe;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An IETF RFC 6838/4855 Media type specifier.
 * <p>
 * Media type specifiers differ from {@link Encoding} instances in two major
 * ways. Firstly, each media type always specifies a major type, which
 * classifies the described as a member of a abstract type category. Secondly,
 * they can include so-called <i>type parameters</i>, which allow for
 * additional type information to be specified, such as used character sets.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6838">IETF RFC 6838</a>
 * @see <a href="https://tools.ietf.org/html/rfc4855">IETF RFC 4855</a>
 * @see <a href="https://www.iana.org/assignments/media-types/media-types.xhtml">IANA Media Types Registry</a>
 */
public final class MediaType implements ToEncoding {
    private static final ConcurrentHashMap<Encoding, MediaType> encodingToMediaType = new ConcurrentHashMap<>();

    private final String original;

    private final String type;
    private final String subtype;
    private final String suffix;
    private final Map<String, String> parameters;

    private final Encoding encoding;

    private MediaType(
        final String original,
        final String type,
        final String subtype,
        final String suffix,
        final Map<String, String> parameters,
        final Encoding encoding
    ) {
        this.original = original;
        this.type = Objects.requireNonNull(type);
        this.subtype = Objects.requireNonNull(subtype);
        this.suffix = suffix;
        this.parameters = Objects.requireNonNullElse(parameters, Collections.emptyMap());
        this.encoding = encoding;
    }

    /**
     * Gets cached media type matching given encoding, or creates a new one
     * using a simple heuristic.
     *
     * @param encoding Encoding associated with desired media type.
     * @return Existing encoding, if any matches {@code name}.
     */
    @ThreadSafe
    public static Optional<MediaType> getOrCreate(final Encoding encoding) {
        Objects.requireNonNull(encoding, "encoding");
        final var mediaType = encodingToMediaType.get(encoding);
        if (mediaType != null) {
            return Optional.of(mediaType);
        }

        final String type;
        final String subtype;
        final String original;
        final Map<String, String> parameters;

        done:
        {
            if (!encoding.isGeneral() && encoding.isTextual()) {
                type = "text";
                final var charset = encoding.charset().orElse(null);
                if (charset != null) {
                    subtype = "plain";
                    final var charsetName = charset.name().toLowerCase();
                    original = "text/plain;charset=" + charsetName;
                    parameters = Map.of("charset", charsetName);
                    break done;
                }
            }
            else {
                type = "application";
            }
            subtype = encoding.name().toLowerCase();
            original = type + "/" + subtype;
            parameters = Map.of();
        }

        final var newMediaType = new MediaType(original, type, subtype, null, parameters, encoding);
        final var oldMediaType = encodingToMediaType.putIfAbsent(encoding, newMediaType);

        return Optional.of(oldMediaType == null ? newMediaType : oldMediaType);
    }

    /**
     * Registers given media-type as the preferred match for the encoding it
     * contains.
     *
     * @param mediaType Media type to register.
     * @return Registered media type.
     * @throws NullPointerException If {@code mediaType} is {@code null} or if
     *                              {@code mediaType} does not have an
     *                              associated encoding.
     */
    @ThreadSafe
    public static MediaType register(final MediaType mediaType) {
        Objects.requireNonNull(mediaType, "mediaType");
        Objects.requireNonNull(mediaType.encoding, "mediaType.encoding()");

        encodingToMediaType.put(mediaType.encoding, mediaType);

        return mediaType;
    }

    /**
     * Major type category, such as "application", "text", "model", "font" or
     * "image".
     *
     * @return Type.
     * @see <a href="https://www.iana.org/assignments/media-types/media-types.xhtml">IANA Media Types Registry</a>
     */
    public String type() {
        return type;
    }

    /**
     * Subtype, excluding any trailing suffix.
     * <p>
     * For example, given the following media types, the highlighted regions
     * will be treated as subtypes:
     * <pre>
     * application/senml+json
     *             ^^^^^
     * application/vnd.apple.pages
     *             ^^^^^^^^^^^^^^^
     * text/html; charset=utf-16
     *      ^^^^
     * </pre>
     *
     * @return Subtype.
     * @see <a href="https://tools.ietf.org/html/rfc6838#section-4.2">IETF RFC 6838, Section 4.2</a>
     */
    public String subtype() {
        return subtype;
    }

    /**
     * Subtype suffix, if any.
     * <p>
     * The suffix, if present, is intended to identity the structural encoding
     * of the subtype, as in "application/senml+xml", where the suffix is "xml".
     *
     * @return Subtype suffix, if any.
     */
    public Optional<String> suffix() {
        return Optional.ofNullable(suffix);
    }

    /**
     * Type parameters.
     *
     * @return Type parameters associated with this type.
     */
    public Map<String, String> parameters() {
        return parameters;
    }

    @Override
    public Encoding toEncoding() {
        return encoding;
    }

    @Override
    public String toString() {
        return original;
    }

    /**
     * Concise Binary Object Representation (CBOR).
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049">RFC 7049</a>
     */
    public static final MediaType APPLICATION_CBOR = register(new MediaType("application/cbor",
        "application",
        "cbor",
        null,
        null,
        Encoding.CBOR));

    /**
     * JavaScript Object Notation (JSON).
     *
     * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
     */
    public static final MediaType APPLICATION_JSON = register(new MediaType(
        "application/json",
        "application",
        "json",
        null,
        null,
        Encoding.JSON));

    /**
     * Extensible Markup Language (XML).
     *
     * @see <a href="https://www.w3.org/TR/xml">W3C XML 1.0</a>
     * @see <a href="https://www.w3.org/TR/xml11">W3C XML 1.1</a>
     */
    public static final MediaType APPLICATION_XML = register(new MediaType(
        "application/xml",
        "application",
        "xml",
        null,
        null,
        Encoding.XML));

    /**
     * Efficient XML Interchange (EXI).
     *
     * @see <a href="https://www.w3.org/TR/exi/">W3C EXI 1.0</a>
     */
    public static final MediaType APPLICATION_EXI = register(new MediaType(
        "application/exi",
        "application",
        "exi",
        null,
        null,
        Encoding.EXI));

    /**
     * Cascading Style Sheets (CSS).
     */
    public static final MediaType TEXT_CSS = register(new MediaType(
        "text/css",
        "text",
        "css",
        null,
        null,
        Encoding.CSS));

    /**
     * Hyper-Text Markup Language (HTML).
     */
    public static final MediaType TEXT_HTML = register(new MediaType(
        "text/html",
        "text",
        "html",
        null,
        null,
        Encoding.HTML));

    /**
     * The Seven-bit ASCII or ISO-646-US character set, which is also the
     * <i>Basic Latin</i> block of the Unicode character set.
     */
    public static final MediaType TEXT_PLAIN_US_ASCII = register(new MediaType(
        "text/plain;charset=us-ascii",
        "text",
        "plain",
        null,
        Map.of("charset", "us-ascii"),
        Encoding.US_ASCII));

    /**
     * The ISO-8869-1 or ISO-LATIN-1 character set.
     */
    public static final MediaType TEXT_PLAIN_ISO_8859_1 = register(new MediaType(
        "text/plain;charset=iso-8859-1",
        "text",
        "plain",
        null,
        Map.of("charset", "iso-8859-1"),
        Encoding.ISO_8859_1));

    /**
     * The UTF-8 Unicode character set.
     */
    public static final MediaType TEXT_PLAIN_UTF_8 = register(new MediaType(
        "text/plain;charset=utf-8",
        "text",
        "plain",
        null,
        Map.of("charset", "utf-8"),
        Encoding.UTF_8));

    /**
     * The UTF-16 Unicode character set, utilizing optional byte order marks to
     * identity 16-bit token endianess.
     */
    public static final MediaType TEXT_PLAIN_UTF_16 = register(new MediaType(
        "text/plain;charset=utf-16",
        "text",
        "plain",
        null,
        Map.of("charset", "utf-16"),
        Encoding.UTF_16));

    /**
     * The UTF-16 Unicode character set with Big-Endian 16-bit tokens.
     */
    public static final MediaType TEXT_PLAIN_UTF16_BE = register(new MediaType(
        "text/plain;charset=utf-16be",
        "text",
        "plain",
        null,
        Map.of("charset", "utf-16be"),
        Encoding.UTF_16BE));

    /**
     * The UTF-16 Unicode character set with Little-Endian 16-bit tokens.
     */
    public static final MediaType TEXT_PLAIN_UTF16_LE = register(new MediaType(
        "text/plain;charset=utf-16le",
        "text",
        "plain",
        null,
        Map.of("charset", "utf-16le"),
        Encoding.UTF_16LE));

    /**
     * Creates or looks up {@link MediaType} from given {@code string}.
     *
     * @param string String to interpret as media type.
     * @return Cached or new {@link MediaType}.
     * @throws NullPointerException If {@code string} is {@code null}.
     * @see <a href="https://tools.ietf.org/html/rfc6838">IETF RFC 6838</a>
     */
    public static MediaType valueOf(final String string) {
        Objects.requireNonNull(string, "string");

        String error = null;
        error:
        {
            final int s4 = string.length();
            // s0 = end of type, s1 = start of suffix, s2 = end of subtype
            int s0 = 0, s1 = 0, s2 = 0, s3;

            // Type.
            char ch;
            if (s0 < s4) {
                ch = string.charAt(s0++);
                if (isNotRestrictedNameFirst(ch)) {
                    error = "Invalid type lead character '" + ch + "'";
                    break error;
                }
                while (s0 < s4) {
                    ch = string.charAt(s0++);
                    if (ch == '/') {
                        break;
                    }
                    if (isNotRestrictedNameChar(ch)) {
                        error = "Invalid type character '" + ch + "'";
                        break error;
                    }
                }
            }
            if (s0 == s4) {
                break error;
            }

            // Subtype.
            int sx = s0;
            outer:
            while (sx < s4) {
                ch = string.charAt(sx++);
                if (isNotRestrictedNameFirst(ch)) {
                    error = "Invalid subtype or facet lead character '" + ch + "'";
                    break error;
                }
                while (sx < s4) {
                    ch = string.charAt(sx);
                    if (ch == '.') {
                        continue outer;
                    }
                    if (ch == '+') {
                        s1 = sx;
                        break outer;
                    }
                    if (ch == ';' || isWhitespace(ch)) {
                        s2 = sx;
                        break outer;
                    }
                    if (isNotRestrictedNameChar(ch)) {
                        error = "Invalid subtype or facet character '" + ch + "'";
                        break error;
                    }
                    sx++;
                }
            }

            // Suffix.
            if (s1 != 0) {
                outer:
                while (s2 < s4) {
                    ch = string.charAt(s2++);
                    if (isNotRestrictedNameFirst(ch)) {
                        error = "Invalid suffix lead character '" + ch + "'";
                        break error;
                    }
                    while (s2 < s4) {
                        ch = string.charAt(s2);
                        if (ch == ';' || isWhitespace(ch)) {
                            break outer;
                        }
                        if (isNotRestrictedNameChar(ch)) {
                            error = "Invalid suffix character '" + ch + "'";
                            break error;
                        }
                        s2++;
                    }
                }
                if (s2 - s1 == 0) {
                    error = "Empty suffixes not permitted";
                    break error;
                }
            }

            final String type;
            final String subtype;
            final String suffix;

            type = string.substring(0, s0 - 1);
            if (s1 == 0) {
                subtype = string.substring(s0, s2);
                suffix = null;
            }
            else {
                subtype = string.substring(s0, s1);
                suffix = string.substring(s1, s2);
            }

            final Map<String, String> parameters;
            s3 = s2;
            if (s3 < s4) {

                // s0 = start of parameter name, s1 = end of parameter name, s3 = start of value, s4 = end of value

                final var parameters0 = new HashMap<String, String>();
                do {
                    s0 = s3;

                    // Skip whitespace.
                    do {
                        ch = string.charAt(s0++);
                        if (!isWhitespace(ch)) {
                            break;
                        }
                    } while (s0 < s4);
                    if (s0 == s4) {
                        break;
                    }

                    if (ch != ';') {
                        break error;
                    }

                    // Parameter name.
                    s1 = s0;
                    ch = string.charAt(s1++);
                    if (isNotRestrictedNameFirst(ch)) {
                        error = "Invalid parameter lead character '" + ch + "'";
                        break error;
                    }
                    while (s1 < s4) {
                        ch = string.charAt(s1);
                        if (ch == '=' || isWhitespace(ch)) {
                            break;
                        }
                        if (isNotRestrictedNameChar(ch)) {
                            error = "Invalid parameter character '" + ch + "'";
                            break error;
                        }
                        s1++;
                    }

                    // Skip whitespace.
                    s2 = s1;
                    do {
                        ch = string.charAt(s2++);
                        if (!isWhitespace(ch)) {
                            break;
                        }
                    } while (s2 < s4);
                    if (s2 == s4) {
                        break error;
                    }

                    // Value.
                    s3 = s2;
                    while (s3 < s4) {
                        ch = string.charAt(s3++);
                        if (ch == ';') {
                            break;
                        }
                    }

                    parameters0.put(string.substring(s0, s1), string.substring(s2, s3));
                } while (s3 < s4);

                parameters = Collections.unmodifiableMap(parameters0);
            }
            else {
                parameters = Collections.emptyMap();
            }

            final Encoding encoding;
            encoding:
            if (suffix == null) {
                if (subtype.regionMatches(true, subtype.length() - 4, "-exi", 0, 4)) {
                    encoding = Encoding.EXI;
                    break encoding;
                }
                final var encoding0 = Encoding.getOrCreate(subtype);
                if (!encoding0.isRegistered() && type.equalsIgnoreCase("text") && subtype.equalsIgnoreCase("plain")) {
                    var charset = parameters.get("charset");
                    if (charset != null) {
                        charset = charset.trim().toUpperCase();
                        if (Charset.isSupported(charset)) {
                            encoding = Encoding.getOrRegister(Charset.forName(charset));
                            break encoding;
                        }
                    }
                }
                encoding = encoding0;
            }
            else {
                encoding = Encoding.getOrCreate(suffix);
            }

            return new MediaType(string, type, subtype, suffix, parameters, encoding);
        }
        if (error == null) {
            error = "expected '<type>/<subtype>[;<parameter-name>=<parameter-value>]*'; got '" + string + "'";
        }
        throw new IllegalArgumentException(error);
    }

    private static boolean isNotRestrictedNameFirst(final char ch) {
        return (ch < '0' || ch > '9') && (ch < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z');
    }

    private static boolean isNotRestrictedNameChar(final char ch) {
        return (ch < '0' || ch > '9') && (ch < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z') &&
            ch != '!' && ch != '#' && ch != '$' && ch != '&' && ch != '-' && ch != '^' && ch != '_';
    }

    private static boolean isWhitespace(final char ch) {
        return ch == '\r' || ch == '\n' || ch == ' ';
    }
}
