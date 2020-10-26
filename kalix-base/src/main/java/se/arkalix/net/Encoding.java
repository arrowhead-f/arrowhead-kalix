package se.arkalix.net;

import se.arkalix.dto.DtoEncoding;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Names a message payload encoding, such as {@link #CBOR} or {@link #JSON}.
 * <p>
 * Encodings are used for representing messages while in transit between
 * systems. They can also be used for representing data that is stored to disk,
 * databases or other media.
 */
public final class Encoding {
    private static final Map<DtoEncoding, Encoding> dtoToEncodingMap;
    private static final Set<Encoding> encodingsWithDtoSupport;

    private final String name;
    private final DtoEncoding dtoEncoding;

    private Encoding(final String name, final DtoEncoding dtoEncoding) {
        this.name = Objects.requireNonNull(name, "Expected name");
        this.dtoEncoding = dtoEncoding;
    }

    /**
     * Gets a set of all encodings for which Kalix DTO support exists. Such
     * encodings can be read and written automatically by the Kalix library.
     *
     * @return Set of all encodings with DTO support.
     * @see se.arkalix.dto
     */
    public static Set<Encoding> allWithDtoSupport() {
        return encodingsWithDtoSupport;
    }

    /**
     * Acquires a cached encoding descriptor matching the given DTO encoding.
     *
     * @param dtoEncoding DTO encoding matching desired encoding descriptor.
     * @return Existing encoding descriptor.
     */
    public static Encoding get(final DtoEncoding dtoEncoding) {
        return dtoToEncodingMap.get(dtoEncoding);
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
     * Gets DTO variant of this descriptor, if any such exists.
     *
     * @return DTO variant of this descriptor.
     * @see se.arkalix.dto
     */
    public Optional<DtoEncoding> asDto() {
        return Optional.ofNullable(dtoEncoding);
    }

    /**
     * @return {@code true} only if DTO support is available for this encoding.
     * @see se.arkalix.dto
     */
    public boolean isDto() {
        return dtoEncoding != null;
    }

    /**
     * Concise Binary Object Representation (CBOR).
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049">RFC 7049</a>
     */
    public static final Encoding CBOR = new Encoding("CBOR", null);

    /**
     * JavaScript Object Notation (JSON).
     *
     * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
     */
    public static final Encoding JSON = new Encoding("JSON", DtoEncoding.JSON);

    /**
     * Extensible Markup Language (XML).
     *
     * @see <a href="https://www.w3.org/TR/xml">W3C XML 1.0</a>
     * @see <a href="https://www.w3.org/TR/xml11">W3C XML 1.1</a>
     */
    public static final Encoding XML = new Encoding("XML", null);

    /**
     * Efficient XML Interchange (EXI).
     *
     * @see <a href="https://www.w3.org/TR/exi/">W3C EXI 1.0</a>
     */
    public static final Encoding EXI = new Encoding("EXI", null);

    /**
     * Resolves {@link Encoding} from given {@code name}.
     *
     * @param name Name to resolve. Case insensitive.
     * @return Cached or new {@link Encoding}.
     */
    public static Encoding valueOf(String name) {
        name = Objects.requireNonNull(name, "Expected name").toUpperCase();
        switch (name) {
        case "CBOR":
            return CBOR;
        case "JSON":
            return JSON;
        case "XML":
            return XML;
        case "EXI":
            return EXI;
        }
        return new Encoding(name, null);
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

    static {
        try {
            final var map = new HashMap<DtoEncoding, Encoding>();
            for (final var field : Encoding.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == Encoding.class) {
                    final var descriptor = (Encoding) field.get(null);
                    if (descriptor.isDto()) {
                        map.put(descriptor.dtoEncoding, descriptor);
                    }
                }
            }
            encodingsWithDtoSupport = map.values().stream().collect(Collectors.toUnmodifiableSet());
            dtoToEncodingMap = map.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        catch (final Exception exception) {
            throw new RuntimeException("DTO encoding set initialization failed", exception);
        }
    }
}
