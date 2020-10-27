package se.arkalix.net;

import se.arkalix.dto.DtoReader;
import se.arkalix.dto.DtoWriter;
import se.arkalix.dto.json.JsonEncoding;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Names a message payload encoding, such as {@link #CBOR} or {@link #JSON}.
 * <p>
 * Encodings are used for representing messages while in transit between
 * systems. They can also be used for representing data that is stored to disk,
 * databases or other media.
 */
public final class Encoding implements ToEncoding {
    private static final ConcurrentHashMap<Encoding, DtoReader> readableEncodings = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Encoding, DtoWriter> writableEncodings = new ConcurrentHashMap<>();

    private final String name;
    private final DtoReader reader;
    private final DtoWriter writer;

    protected Encoding(final String name, final DtoReader reader, final DtoWriter writer) {
        this.name = Objects.requireNonNull(name, "name");
        this.reader = reader;
        this.writer = writer;

        if (reader != null) {
            readableEncodings.put(this, reader);
        }
        if (writer != null) {
            writableEncodings.put(this, writer);
        }
    }

    public static <T extends DtoReader & DtoWriter> Encoding register(final String name, final T encoding) {
        return register(name, encoding, encoding);
    }

    public static Encoding register(final String name, final DtoReader reader, final DtoWriter writer) {
        return new Encoding(name, reader, writer);
    }

    /**
     * Gets set of all encodings for which Kalix DTO read support
     * exists. Such encodings can be read automatically by the Kalix library.
     *
     * @return Set of all encodings with DTO read support.
     * @see se.arkalix.dto
     */
    public static Set<Encoding> allReadable() {
        return readableEncodings.keySet();
    }

    /**
     * Gets set of all encodings for which Kalix DTO write support
     * exists. Such encodings can be written automatically by the Kalix library.
     *
     * @return Set of all encodings with DTO write support.
     * @see se.arkalix.dto
     */
    public static Set<Encoding> allWritable() {
        return writableEncodings.keySet();
    }

    /**
     * Either acquires a cached encoding descriptor matching the given name, or
     * creates a new descriptor.
     *
     * @param name Desired encoding descriptor name.
     * @return New or existing encoding descriptor.
     */
    public static Encoding getOrCreate(final String name) {
        return valueOf(name);
    }

    /**
     * @return Encoding identifier.
     */
    public String name() {
        return name;
    }

    /**
     * Gets {@link DtoReader} associated with this encoding, if any.
     *
     * @return Reader associated with this encoding, if any.
     */
    public Optional<DtoReader> reader() {
        return Optional.ofNullable(reader);
    }

    /**
     * Gets {@link DtoWriter} associated with this encoding, if any.
     *
     * @return Writer associated with this encoding, if any.
     */
    public Optional<DtoWriter> writer() {
        return Optional.ofNullable(writer);
    }

    /**
     * Determines whether this encoding can be read.
     * <p>
     * In other words, if this method returns {@code true}, then {@link
     * #reader()} is guaranteed to return a non-empty result.
     *
     * @return {@code true} only if DTO read support is available for this
     * encoding.
     * @see se.arkalix.dto
     */
    public boolean isReadable() {
        return reader != null;
    }

    /**
     * Determines whether this encoding can be written.
     * <p>
     * In other words, if this method returns {@code true}, then {@link
     * #writer()} is guaranteed to return a non-empty result.
     *
     * @return {@code true} only if DTO write support is available for this
     * encoding.
     * @see se.arkalix.dto
     */
    public boolean isWritable() {
        return writer != null;
    }

    /**
     * Concise Binary Object Representation (CBOR).
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049">RFC 7049</a>
     */
    public static final Encoding CBOR = register("CBOR", null, null);

    /**
     * JavaScript Object Notation (JSON).
     *
     * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
     */
    public static final Encoding JSON = register("JSON", JsonEncoding.instance());

    /**
     * Extensible Markup Language (XML).
     *
     * @see <a href="https://www.w3.org/TR/xml">W3C XML 1.0</a>
     * @see <a href="https://www.w3.org/TR/xml11">W3C XML 1.1</a>
     */
    public static final Encoding XML = register("XML", null, null);

    /**
     * Efficient XML Interchange (EXI).
     *
     * @see <a href="https://www.w3.org/TR/exi/">W3C EXI 1.0</a>
     */
    public static final Encoding EXI = register("EXI", null, null);

    /**
     * Resolves {@link Encoding} from given {@code name}.
     *
     * @param name Name to resolve. Case insensitive.
     * @return Cached or new {@link Encoding}.
     */
    public static Encoding valueOf(String name) {
        name = Objects.requireNonNull(name, "Expected name").toUpperCase();
        switch (name) {
        case "CBOR": return CBOR;
        case "JSON": return JSON;
        case "XML": return XML;
        case "EXI": return EXI;
        }
        return register(name, null, null);
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
}
