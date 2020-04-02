package se.arkalix.descriptor;

import se.arkalix.dto.DtoEncoding;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Names a type of message payload encoding.
 */
public final class EncodingDescriptor {
    private static final Set<EncodingDescriptor> DTO_ENCODINGS;

    private final String name;
    private final DtoEncoding dtoEncoding;

    private EncodingDescriptor(final String name, final DtoEncoding dtoEncoding) {
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
    public static Set<EncodingDescriptor> dtoEncodings() {
        return DTO_ENCODINGS;
    }

    /**
     * Either acquires a cached encoding descriptor matching the given name, or
     * creates a new descriptor.
     *
     * @param name Desired encoding descriptor name.
     * @return New or existing encoding descriptor.
     */
    public static EncodingDescriptor getOrCreate(final String name) {
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
    public Optional<DtoEncoding> asDtoEncoding() {
        return Optional.ofNullable(dtoEncoding);
    }

    /**
     * @return {@code true} only if DTO support is available for this encoding.
     * @see se.arkalix.dto
     */
    public boolean isDtoEncoding() {
        return dtoEncoding != null;
    }

    /**
     * Concise Binary Object Representation (CBOR).
     *
     * @see <a href="https://tools.ietf.org/html/rfc7049">RFC 7049</a>
     */
    public static final EncodingDescriptor CBOR = new EncodingDescriptor("CBOR", null);

    /**
     * JavaScript Object Notation (JSON).
     *
     * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
     */
    public static final EncodingDescriptor JSON = new EncodingDescriptor("JSON", DtoEncoding.JSON);

    /**
     * Extensible Markup Language (XML).
     *
     * @see <a href="https://www.w3.org/TR/xml">W3C XML 1.0</a>
     * @see <a href="https://www.w3.org/TR/xml11">W3C XML 1.1</a>
     */
    public static final EncodingDescriptor XML = new EncodingDescriptor("XML", null);

    /**
     * Efficient XML Interchange (EXI).
     *
     * @see <a href="https://www.w3.org/TR/exi/">W3C EXI 1.0</a>
     */
    public static final EncodingDescriptor EXI = new EncodingDescriptor("EXI", null);

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

    static {
        try {
            final var dtoEncodings = new ArrayList<EncodingDescriptor>();
            for (final var field : EncodingDescriptor.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == EncodingDescriptor.class) {
                    final var descriptor = (EncodingDescriptor) field.get(null);
                    if (descriptor.isDtoEncoding()) {
                        dtoEncodings.add(descriptor);
                    }
                }
            }
            DTO_ENCODINGS = dtoEncodings.stream().collect(Collectors.toUnmodifiableSet());
        }
        catch (final Exception exception) {
            throw new RuntimeException("DTO encoding set initialization failed", exception);
        }
    }
}
