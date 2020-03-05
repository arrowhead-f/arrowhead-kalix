package eu.arrowhead.kalix.descriptor;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Describes a type of message payload encoding.
 */
public class EncodingDescriptor {
    private final String name;
    private final String mediaTypePatternString;

    private Pattern mediaTypePattern;

    private EncodingDescriptor(final String name, final String mediaTypePattern) {
        this.name = Objects.requireNonNull(name, "Expected name");
        this.mediaTypePatternString = mediaTypePattern != null
            ? mediaTypePattern

            // RFC 6838, Section 4.2.8 states that '"+suffix" constructs for
            // as-yet unregistered structured syntaxes SHOULD NOT be used,
            // given the possibility of conflicts with future suffix
            // definitions.' For this reason, we cannot make any more
            // assumptions about what a suitable MIME-type should look like
            // than this.
            : "^application\\/" + name.toLowerCase() + "$";
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
     * @param mediaType Media type to test, such as {@code "application/json"}
     *                  or {@code "application/xmpp+xml"}.
     * @return {@code true} only if this encoding could, theoretically, be used
     * to encode or decode data to/from the provided media type.
     */
    public boolean usedByMediaType(final CharSequence mediaType) {
        if (mediaTypePattern == null) {
            mediaTypePattern = Pattern.compile(mediaTypePatternString, Pattern.CASE_INSENSITIVE);
        }
        return mediaTypePattern.matcher(mediaType).find();
    }

    /**
     * Concise Binary Object Representation (CBOR).
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049">RFC 7049</a>
     */
    public static final EncodingDescriptor CBOR = new EncodingDescriptor("CBOR",
        "^application\\/(?:[^\\+]*\\+)?cbor$");

    /**
     * JavaScript Object Notation (JSON).
     *
     * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
     */
    public static final EncodingDescriptor JSON = new EncodingDescriptor("JSON",
        "^application\\/(?:[^\\+]*\\+)?json$");

    /**
     * Extensible Markup Language (XML).
     *
     * @see <a href="https://www.w3.org/TR/xml">W3C XML 1.0</a>
     * @see <a href="https://www.w3.org/TR/xml11">W3C XML 1.1</a>
     */
    public static final EncodingDescriptor XML = new EncodingDescriptor("XML",
        "^(?:(?:application)|(?:text))\\/(?:[^\\+]*\\+)?xml$");

    /**
     * Efficient XML Interchange (EXI).
     *
     * @see <a href="https://www.w3.org/TR/exi/">W3C EXI 1.0</a>
     */
    public static final EncodingDescriptor EXI = new EncodingDescriptor("EXI",
        "^application\\/(?:[^-]*-)?exi$");

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
        return new EncodingDescriptor(name, null);
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
