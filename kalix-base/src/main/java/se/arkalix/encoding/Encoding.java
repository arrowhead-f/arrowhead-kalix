package se.arkalix.encoding;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Names any out of a set of known <i>encodings</i>.
 * <p>
 * An encoding is understood to be set of rules used for assigning meaning to
 * certain bit patterns in strings of bytes, which then can be followed to
 * construct Java objects from such bit patterns and to turn Java objects into
 * bit patterns. Each encoding can be <i>known</i> to be general-purpose,
 * textual and/or a character set.
 * <p>
 * A general-purpose encoding can represent arbitrary data structures, such as
 * associative arrays (a.k.a. maps or dictionaries) and lists, with the syntax
 * already defined for the encoding. Examples of such encodings include
 * {@link #JSON}, {@link #XML} and {@code CBOR}. Encodings not being
 * general-purpose are interpreted as being application-specific, such as
 * {@link #HTML} or {@link #CSS}.
 * <p>
 * A textual encoding is such that is fully compatible with one or more
 * character sets, while a binary encoding is not. Textual encodings can, as a
 * consequence, be treated as strings of text, even if they are general-
 * purpose. Note that only the encodings with a single associated character set
 * might make that available via the {@link #charset()} method. {@link #JSON}
 * and YAML match this definition, as well as plain character sets, such as
 * {@link #US_ASCII} or {@link #UTF_8}. However, {@link #XML} does not, as it
 * can be encoded using both UTF-8 and UTF-16.
 */
public final class Encoding implements ToEncoding {
    private static final ConcurrentHashMap<String, Encoding> nameToEncoding = new ConcurrentHashMap<>();

    private final String name;
    private final boolean isRegistered;
    private final boolean isGeneral;
    private final boolean isTextual;
    private final boolean isCharset;
    private final Charset charset;

    private Encoding(final String name) {
        this.name = Objects.requireNonNull(name, "name");
        this.isRegistered = false;
        this.isGeneral = false;
        this.isTextual = false;
        this.isCharset = false;
        this.charset = null;
    }

    private Encoding(final Registration registration) {
        this.name = Objects.requireNonNull(registration.name, "name");
        this.isRegistered = true;
        this.isGeneral = registration.isGeneral;
        this.isTextual = registration.isTextual || registration.charset != null;
        this.isCharset = registration.isCharset || registration.charset != null;
        this.charset = registration.charset;
    }

    /**
     * Gets cached encoding identifier matching given name, if any.
     *
     * @param name String name of desired encoding identifier.
     * @return Existing encoding, if any matches {@code name}.
     */
    public static Optional<Encoding> get(final String name) {
        return Optional.ofNullable(nameToEncoding.get(Objects.requireNonNull(name, "name").toUpperCase()));
    }

    /**
     * Either acquires a cached encoding matching the given name, or creates a
     * new unregistered encoding.
     *
     * @param name String name of desired encoding identifier.
     * @return New or existing encoding.
     * @throws NullPointerException If {@code name} is {@code null}.
     */
    public static Encoding getOrCreate(final String name) {
        return valueOf(name);
    }

    /**
     * Either acquires a cached character set encoding matching the given name,
     * or registers and returns the given character set.
     *
     * @param charset Character set to get or register.
     * @return New or cached encoding.
     * @throws NullPointerException If {@code charset} is {@code null}.
     */
    public static Encoding getOrRegister(final Charset charset) {
        final var name = Objects.requireNonNull(charset, "charset")
            .name()
            .toUpperCase();

        return nameToEncoding.computeIfAbsent(name, name0 ->
            new Encoding(new Registration().name(name0).charset(charset)));
    }

    /**
     * Registers given character set as an encoding.
     *
     * @param charset Character set to register.
     * @return New encoding.
     * @throws EncodingAlreadyRegistered If another encoding with the same name
     *                                   has already been explicitly registered.
     * @throws NullPointerException      If {@code charset} is {@code null}.
     */
    public static Encoding register(final Charset charset) {
        final var name = Objects.requireNonNull(charset, "charset")
            .name()
            .toUpperCase();

        return nameToEncoding.compute(name, (name0, encoding) -> {
            if (encoding != null && encoding.isRegistered()) {
                throw new EncodingAlreadyRegistered(encoding);
            }
            return new Encoding(new Registration()
                .name(name)
                .textual()
                .charset()
                .charset(charset));
        });
    }

    /**
     * Completes given encoding registration.
     *
     * @param registration Encoding registration to complete.
     * @return New encoding.
     * @throws EncodingAlreadyRegistered If another encoding with the same name
     *                                   has already been explicitly registered.
     * @throws NullPointerException      If {@code registration} is {@code null}.
     */
    public static Encoding register(final Registration registration) {
        final var name0 = Objects.requireNonNull(registration.name, "name").toUpperCase();
        return nameToEncoding.compute(name0, (name1, encoding) -> {
            if (encoding != null && encoding.isRegistered()) {
                throw new EncodingAlreadyRegistered(encoding);
            }
            return new Encoding(registration);
        });
    }

    /**
     * Name associated with this encoding.
     *
     * @return Encoding identifier.
     */
    public String name() {
        return name;
    }

    /**
     * Determines weather or not this encoding has been explicitly registered
     * with a call to {@link #register(Registration)} or
     * {@link #register(Charset)}.
     *
     * @return {@code true} only if this encoding has been explicitly
     * registered.
     */
    public boolean isRegistered() {
        return isRegistered;
    }

    /**
     * Determines weather or not this encoding is <i>known</i> to be
     * general-purpose, as defined in the {@link Encoding class description}.
     *
     * @return {@code true} only if this encoding is known to be general-
     * purpose.
     */
    public boolean isGeneral() {
        return isGeneral;
    }

    /**
     * Determines weather or not this encoding is <i>known</i> to be
     * textual, as defined in the {@link Encoding class description}.
     * <p>
     * This method returning {@code true} does <i>not</i> guarantee that the
     * {@link #charset()} method will return a non-empty result.
     *
     * @return {@code true} only if this encoding is known to be textual.
     */
    public boolean isTextual() {
        return isTextual;
    }

    /**
     * Determines weather or not this encoding <i>is</i> a character set.
     * <p>
     * This method returning {@code true} does <i>not</i> guarantee that the
     * {@link #charset()} method will return a non-empty result. Even though
     * this encoding is known to be a character set, it is not guaranteed that
     * this system knows how to interpret that character set.
     *
     * @return {@code true} only if this encoding is a charset.
     */
    public boolean isCharset() {
        return isCharset;
    }

    /**
     * Gets the one character set associated with this encoding.
     * <p>
     * This method might return an empty result even if this encoding {@link
     * #isTextual() is textual} or {@link #isCharset() represents only a
     * character set}. Reasons could include the textual encoding being
     * representable with more than one encoding, or the current system not
     * having support for the character set in question.
     *
     * @return Character set associated with this encoding, if any.
     */
    public Optional<Charset> charset() {
        return Optional.ofNullable(charset);
    }

    /**
     * No encoding at all. This encoding is to be used when the encoding is
     * completely unknown or is not relevant.
     * <p>
     * This {@link Encoding} instance cannot be acquired via {@link
     * #get(String)}, {@link #getOrCreate(String)} or {@link
     * #getOrRegister(Charset)}.
     */
    public static final Encoding NONE = new Encoding("none");

    /**
     * Concise Binary Object Representation (CBOR).
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049">RFC 7049</a>
     */
    public static final Encoding CBOR = register(new Registration()
        .name("CBOR")
        .general());

    /**
     * Cascading Style Sheet (CSS).
     */
    public static final Encoding CSS = register(new Registration()
        .name("CSS")
        .textual());

    /**
     * Hyper-Text Markup Language (HTML).
     */
    public static final Encoding HTML = register(new Registration()
        .name("HTML")
        .textual());

    /**
     * JavaScript Object Notation (JSON).
     *
     * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
     */
    public static final Encoding JSON = register(new Registration()
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
    public static final Encoding XML = register(new Registration()
        .name("XML")
        .general()
        .textual());

    /**
     * Efficient XML Interchange (EXI).
     *
     * @see <a href="https://www.w3.org/TR/exi/">W3C EXI 1.0</a>
     */
    public static final Encoding EXI = register(new Registration()
        .name("EXI")
        .general());

    /**
     * The Seven-bit ASCII or ISO-646-US character set, which is also the
     * <i>Basic Latin</i> block of the Unicode character set.
     */
    public static final Encoding US_ASCII = register(StandardCharsets.US_ASCII);

    /**
     * The ISO-8869-1 or ISO-LATIN-1 character set.
     */
    public static final Encoding ISO_8859_1 = register(StandardCharsets.ISO_8859_1);

    /**
     * The UTF-8 Unicode character set.
     */
    public static final Encoding UTF_8 = register(StandardCharsets.UTF_8);

    /**
     * The UTF-16 Unicode character set, utilizing optional byte order marks to
     * identity 16-bit token endianess.
     */
    public static final Encoding UTF_16 = register(StandardCharsets.UTF_16);

    /**
     * The UTF-16 Unicode character set with Big-Endian 16-bit tokens.
     */
    public static final Encoding UTF_16BE = register(StandardCharsets.UTF_16BE);

    /**
     * The UTF-16 Unicode character set with Little-Endian 16-bit tokens.
     */
    public static final Encoding UTF_16LE = register(StandardCharsets.UTF_16LE);

    /**
     * Resolves {@link Encoding} from given {@code name}.
     *
     * @param name Name to resolve. Case insensitive.
     * @return Cached or new {@link Encoding}.
     * @throws NullPointerException If {@code name} is {@code null}.
     */
    public static Encoding valueOf(String name) {
        name = Objects.requireNonNull(name, "name").toUpperCase();
        return nameToEncoding.computeIfAbsent(name, Encoding::new);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final var encoding = (Encoding) o;
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

    @Override
    public Encoding toEncoding() {
        return this;
    }

    /**
     * An encoding registration form.
     */
    public static class Registration {
        private String name;
        private boolean isGeneral = false;
        private boolean isTextual = false;
        private boolean isCharset = false;
        private Charset charset = null;

        /**
         * Sets encoding name. <b>Must be specified.</b>
         *
         * @param name Case-independent encoding name. Prefer uppercase.
         * @return This registration form.
         */
        public Registration name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Marks registered encoding as known to be general-purpose.
         *
         * @return This registration form.
         */
        public Registration general() {
            this.isGeneral = true;
            return this;
        }

        /**
         * Marks registered encoding as known to be textual.
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
         * encoding.
         *
         * @param charset Character set to associate with this encoding.
         * @return This registration form.
         */
        public Registration charset(final Charset charset) {
            this.charset = charset;
            return this;
        }
    }
}
