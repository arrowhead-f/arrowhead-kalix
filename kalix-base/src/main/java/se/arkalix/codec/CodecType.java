package se.arkalix.codec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Identifies one out of a set of known <i>codecs</i>.
 * <p>
 * An codec is understood to be set of rules used for assigning meaning to
 * certain bit patterns in strings of bytes, which then can be followed to
 * construct Java objects from such bit patterns and to turn Java objects into
 * bit patterns. Each codec can be <i>known</i> to be general-purpose,
 * textual and/or a character set.
 * <p>
 * A general-purpose codec can represent arbitrary data structures, such as
 * associative arrays (a.k.a. maps or dictionaries) and lists, with the syntax
 * already defined for the codec. Examples of such codecs include
 * {@link #JSON}, {@link #XML} and {@code CBOR}. Codecs not being
 * general-purpose are interpreted as being application-specific, such as
 * {@link #HTML} or {@link #CSS}.
 * <p>
 * A textual codec is such that is fully compatible with one or more
 * character sets, while a binary codec is not. Textual codecs can, as a
 * consequence, be treated as strings of text, even if they are general-
 * purpose. Note that only the codecs with a single associated character set
 * might make that available via the {@link #charset()} method. {@link #JSON}
 * and YAML match this definition, as well as plain character sets, such as
 * {@link #US_ASCII} or {@link #UTF_8}. However, {@link #XML} does not, as it
 * can be encoded using both UTF-8 and UTF-16.
 */
public final class CodecType implements ToCodecType {
    private static final ConcurrentHashMap<String, CodecType> nameToCodec = new ConcurrentHashMap<>();

    private final String name;
    private final boolean isRegistered;
    private final boolean isGeneral;
    private final boolean isTextual;
    private final boolean isCharset;
    private final Charset charset;

    private CodecType(final String name) {
        this.name = Objects.requireNonNull(name, "name");
        this.isRegistered = false;
        this.isGeneral = false;
        this.isTextual = false;
        this.isCharset = false;
        this.charset = null;
    }

    private CodecType(final Registration registration) {
        this.name = Objects.requireNonNull(registration.name, "name");
        this.isRegistered = true;
        this.isGeneral = registration.isGeneral;
        this.isTextual = registration.isTextual || registration.charset != null;
        this.isCharset = registration.isCharset || registration.charset != null;
        this.charset = registration.charset;
    }

    /**
     * Gets cached codec identifier matching given name, if any.
     *
     * @param name String name of desired codec identifier.
     * @return Existing codec, if any matches {@code name}.
     */
    public static Optional<CodecType> get(final String name) {
        return Optional.ofNullable(nameToCodec.get(Objects.requireNonNull(name, "name").toUpperCase()));
    }

    /**
     * Either acquires a cached codec matching the given name, or creates a
     * new unregistered codec.
     *
     * @param name String name of desired codec identifier.
     * @return New or existing codec.
     * @throws NullPointerException If {@code name} is {@code null}.
     */
    public static CodecType getOrCreate(final String name) {
        return valueOf(name);
    }

    /**
     * Either acquires a cached character set codec matching the given name,
     * or registers and returns the given character set.
     *
     * @param charset Character set to get or register.
     * @return New or cached codec.
     * @throws NullPointerException If {@code charset} is {@code null}.
     */
    public static CodecType getOrRegister(final Charset charset) {
        final var name = Objects.requireNonNull(charset, "charset")
            .name()
            .toUpperCase();

        return nameToCodec.computeIfAbsent(name, name0 ->
            new CodecType(new Registration().name(name0).charset(charset)));
    }

    /**
     * Registers given character set as an codec.
     *
     * @param charset Character set to register.
     * @return New codec.
     * @throws CodecAlreadyRegistered If another codec with the same name
     *                                   has already been explicitly registered.
     * @throws NullPointerException      If {@code charset} is {@code null}.
     */
    public static CodecType register(final Charset charset) {
        final var name = Objects.requireNonNull(charset, "charset")
            .name()
            .toUpperCase();

        return nameToCodec.compute(name, (name0, codec) -> {
            if (codec != null && codec.isRegistered()) {
                throw new CodecAlreadyRegistered(codec);
            }
            return new CodecType(new Registration()
                .name(name)
                .textual()
                .charset()
                .charset(charset));
        });
    }

    /**
     * Completes given codec registration.
     *
     * @param registration Codec registration to complete.
     * @return New codec.
     * @throws CodecAlreadyRegistered If another codec with the same name
     *                                   has already been explicitly registered.
     * @throws NullPointerException      If {@code registration} is {@code null}.
     */
    public static CodecType register(final Registration registration) {
        final var name0 = Objects.requireNonNull(registration.name, "name").toUpperCase();
        return nameToCodec.compute(name0, (name1, codec) -> {
            if (codec != null && codec.isRegistered()) {
                throw new CodecAlreadyRegistered(codec);
            }
            return new CodecType(registration);
        });
    }

    /**
     * Name associated with this codec type.
     *
     * @return Codec name.
     */
    public String name() {
        return name;
    }

    /**
     * Determines weather or not this codec has been explicitly registered
     * with a call to {@link #register(Registration)} or
     * {@link #register(Charset)}.
     *
     * @return {@code true} only if this codec has been explicitly
     * registered.
     */
    public boolean isRegistered() {
        return isRegistered;
    }

    /**
     * Determines weather or not this codec is <i>known</i> to be
     * general-purpose, as defined in the {@link CodecType class description}.
     *
     * @return {@code true} only if this codec is known to be general-
     * purpose.
     */
    public boolean isGeneral() {
        return isGeneral;
    }

    /**
     * Determines weather or not this codec is <i>known</i> to be
     * textual, as defined in the {@link CodecType class description}.
     * <p>
     * This method returning {@code true} does <i>not</i> guarantee that the
     * {@link #charset()} method will return a non-empty result.
     *
     * @return {@code true} only if this codec is known to be textual.
     */
    public boolean isTextual() {
        return isTextual;
    }

    /**
     * Determines weather or not this codec <i>is</i> a character set.
     * <p>
     * This method returning {@code true} does <i>not</i> guarantee that the
     * {@link #charset()} method will return a non-empty result. Even though
     * this codec is known to be a character set, it is not guaranteed that
     * this system knows how to interpret that character set.
     *
     * @return {@code true} only if this codec is a charset.
     */
    public boolean isCharset() {
        return isCharset;
    }

    /**
     * Gets the one character set associated with this codec.
     * <p>
     * This method might return an empty result even if this codec {@link
     * #isTextual() is textual} or {@link #isCharset() represents only a
     * character set}. Reasons could include the textual codec being
     * representable with more than one codec, or the current system not
     * having support for the character set in question.
     *
     * @return Character set associated with this codec, if any.
     */
    public Optional<Charset> charset() {
        return Optional.ofNullable(charset);
    }

    /**
     * No codec at all. This codec is to be used when the codec is
     * completely unknown or is not relevant.
     * <p>
     * This {@link CodecType} instance cannot be acquired via {@link
     * #get(String)}, {@link #getOrCreate(String)} or {@link
     * #getOrRegister(Charset)}.
     */
    public static final CodecType NONE = new CodecType("none");

    /**
     * Concise Binary Object Representation (CBOR).
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049">RFC 7049</a>
     */
    public static final CodecType CBOR = register(new Registration()
        .name("CBOR")
        .general());

    /**
     * Cascading Style Sheet (CSS).
     */
    public static final CodecType CSS = register(new Registration()
        .name("CSS")
        .textual());

    /**
     * Hyper-Text Markup Language (HTML).
     */
    public static final CodecType HTML = register(new Registration()
        .name("HTML")
        .textual());

    /**
     * JavaScript Object Notation (JSON).
     *
     * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
     */
    public static final CodecType JSON = register(new Registration()
        .name("JSON")
        .general()
        .textual()
        .charset(StandardCharsets.UTF_8));

    /**
     * Extensible Markup Language (XML).
     *
     * @see <a href="https://www.w3.org/TR/xml">W3C XML 1.0</a>
     * @see <a href="https://www.w3.org/TR/xml11">W3C XML 1.1</a>
     */
    public static final CodecType XML = register(new Registration()
        .name("XML")
        .general()
        .textual());

    /**
     * Efficient XML Interchange (EXI).
     *
     * @see <a href="https://www.w3.org/TR/exi/">W3C EXI 1.0</a>
     */
    public static final CodecType EXI = register(new Registration()
        .name("EXI")
        .general());

    /**
     * The Seven-bit ASCII or ISO-646-US character set, which is also the
     * <i>Basic Latin</i> block of the Unicode character set.
     */
    public static final CodecType US_ASCII = register(StandardCharsets.US_ASCII);

    /**
     * The ISO-8869-1 or ISO-LATIN-1 character set.
     */
    public static final CodecType ISO_8859_1 = register(StandardCharsets.ISO_8859_1);

    /**
     * The UTF-8 Unicode character set.
     */
    public static final CodecType UTF_8 = register(StandardCharsets.UTF_8);

    /**
     * The UTF-16 Unicode character set, utilizing optional byte order marks to
     * identity 16-bit token endianess.
     */
    public static final CodecType UTF_16 = register(StandardCharsets.UTF_16);

    /**
     * The UTF-16 Unicode character set with Big-Endian 16-bit tokens.
     */
    public static final CodecType UTF_16BE = register(StandardCharsets.UTF_16BE);

    /**
     * The UTF-16 Unicode character set with Little-Endian 16-bit tokens.
     */
    public static final CodecType UTF_16LE = register(StandardCharsets.UTF_16LE);

    /**
     * Resolves {@link CodecType} from given {@code name}.
     *
     * @param name Name to resolve. Case insensitive.
     * @return Cached or new {@link CodecType}.
     * @throws NullPointerException If {@code name} is {@code null}.
     */
    public static CodecType valueOf(String name) {
        name = Objects.requireNonNull(name, "name").toUpperCase();
        return nameToCodec.computeIfAbsent(name, CodecType::new);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final var codec = (CodecType) o;
        return name.equals(codec.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public CodecType toCodecType() {
        return this;
    }

    /**
     * An codec registration form.
     */
    public static class Registration {
        private String name;
        private boolean isGeneral = false;
        private boolean isTextual = false;
        private boolean isCharset = false;
        private Charset charset = null;

        /**
         * Sets codec name. <b>Must be specified.</b>
         *
         * @param name Case-independent codec name. Prefer uppercase.
         * @return This registration form.
         */
        public Registration name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Marks registered codec as known to be general-purpose.
         *
         * @return This registration form.
         */
        public Registration general() {
            this.isGeneral = true;
            return this;
        }

        /**
         * Marks registered codec as known to be textual.
         *
         * @return This registration form.
         */
        public Registration textual() {
            this.isTextual = true;
            return this;
        }

        private Registration charset() {
            this.isCharset = true;
            return this;
        }

        /**
         * Sets the single character set to be associated with the registered
         * codec.
         *
         * @param charset Character set to associate with this codec.
         * @return This registration form.
         */
        public Registration charset(final Charset charset) {
            this.charset = charset;
            return this;
        }
    }
}
