package eu.arrowhead.kalix.descriptor;

import eu.arrowhead.kalix.dto.DataEncoding;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Describes a type of message payload encoding.
 */
public class EncodingDescriptor {
    private final String name;
    private final DataEncoding dataEncoding;

    private String mediaTypePatternString;
    private Pattern mediaTypePattern;

    private EncodingDescriptor(final String name, final DataEncoding dataEncoding, final String mediaTypePattern) {
        this.name = Objects.requireNonNull(name, "Expected name");
        this.dataEncoding = dataEncoding;
        this.mediaTypePatternString = mediaTypePattern;
    }

    /**
     * Either acquires a cached encoding descriptor matching the given name or
     * creates a new one.
     *
     * @param name Desired encoding descriptor name.
     * @return New or existing encoding descriptor.
     */
    public EncodingDescriptor getOrCreate(final String name) {
        return valueOf(name);
    }

    /**
     * @return Encoding identifier.
     */
    public String name() {
        return name;
    }

    /**
     * Gets DTO variant of this descriptor, if any such exists.
     *
     * @return DTO variant of this descriptor.
     * @see eu.arrowhead.kalix.dto
     */
    public Optional<DataEncoding> asDataEncoding() {
        return Optional.ofNullable(dataEncoding);
    }

    /**
     * Gets {@code Pattern} useful for determine if given RFC 6838 media types,
     * such as {@code "application/json"} or {@code "application/xmpp+xml"},
     * are relying on this encoding.
     *
     * @return Regular expression pattern matching compatible media types.
     * @see <a href="https://tools.ietf.org/html/rfc6838">RFC 6838</a>
     */
    public Pattern asMediaTypePattern() {
        if (mediaTypePattern == null) {
            if (mediaTypePatternString == null) {
                mediaTypePatternString = "^application\\/" + name.toLowerCase() + "$";
            }
            mediaTypePattern = Pattern.compile(mediaTypePatternString, Pattern.CASE_INSENSITIVE);
        }
        return mediaTypePattern;
    }

    /**
     * Concise Binary Object Representation (CBOR).
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049">RFC 7049</a>
     */
    public static final EncodingDescriptor CBOR = new EncodingDescriptor("CBOR",
        null, "^application\\/(?:[^\\+]*\\+)?cbor$");

    /**
     * JavaScript Object Notation (JSON).
     *
     * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
     */
    public static final EncodingDescriptor JSON = new EncodingDescriptor("JSON",
        DataEncoding.JSON, "^application\\/(?:[^\\+]*\\+)?json$");

    /**
     * Extensible Markup Language (XML).
     *
     * @see <a href="https://www.w3.org/TR/xml">W3C XML 1.0</a>
     * @see <a href="https://www.w3.org/TR/xml11">W3C XML 1.1</a>
     */
    public static final EncodingDescriptor XML = new EncodingDescriptor("XML",
        null, "^(?:(?:application)|(?:text))\\/(?:[^\\+]*\\+)?xml$");

    /**
     * Efficient XML Interchange (EXI).
     *
     * @see <a href="https://www.w3.org/TR/exi/">W3C EXI 1.0</a>
     */
    public static final EncodingDescriptor EXI = new EncodingDescriptor("EXI",
        null, "^application\\/(?:[^-]*-)?exi$");

    /**
     * Resolves {@link EncodingDescriptor} from given {@code name}.
     *
     * @param name Name to resolve. Case insensitive.
     * @return Cached or new {@link EncodingDescriptor}.
     */
    public static EncodingDescriptor valueOf(String name) {
        name = Objects.requireNonNull(name, "Expected name").toUpperCase();
        switch (name) {
        case "CBOR": return CBOR;
        case "JSON": return JSON;
        case "XML": return XML;
        case "EXI": return EXI;
        }
        return new EncodingDescriptor(name, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final var encoding = (EncodingDescriptor) o;
        return name.equals(encoding.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
