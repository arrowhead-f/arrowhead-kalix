package se.arkalix.codec;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A wrapper allowing for lists of encodable objects to be encoded together.
 * <p>
 * Not all codecs support anonymous lists, and not all codecs that do are
 * supported by this class. Use {@link #supportedEncodings()} to get a set of
 * all supported codecs. As a rule, all codecs supported by the Kalix DTO
 * package should be supported here, unless the codec in question does not have
 * an anonymous list type, such as in the case of XML.
 */
public class MultiEncodableForLists implements MultiEncodable {
    private static final Set<CodecType> supportedEncodings = Set.of(CodecType.JSON);

    private final List<? extends MultiEncodable> items;

    /**
     * Gets set of codecs supported by instances of this class.
     *
     * @return Set of supported codecs.
     */
    public static Set<CodecType> supportedEncodings() {
        return supportedEncodings;
    }

    private MultiEncodableForLists(final List<? extends MultiEncodable> items) {
        this.items = Objects.requireNonNullElse(items, List.of());
    }

    /**
     * Creates new list encoder, wrapping given items.
     *
     * @param items Items to be wrapped.
     * @return New {@link MultiEncodableForLists}.
     */
    public static MultiEncodableForLists of(final List<? extends MultiEncodable> items) {
        return new MultiEncodableForLists(items);
    }

    /**
     * Creates new list encoder, wrapping given items.
     *
     * @param items Items to be wrapped.
     * @return New {@link MultiEncodableForLists}.
     */
    public static MultiEncodableForLists of(final MultiEncodable... items) {
        return of(List.of(items));
    }

    @Override
    public Encodable encodableFor(final CodecType codecType) {
        if (codecType == CodecType.JSON) {
            return encodableForJson();
        }
        else {
            throw new CodecUnsupported(codecType);
        }
    }

    /**
     * Gets {@link Encodable} producing JSON arrays.
     */
    public Encodable encodableForJson() {
        return writer -> {
            if (writer == null) {
                throw new NullPointerException("writer");
            }

            writer.write((byte) '[');

            if (items.size() > 0) {
                items.get(0).encodableFor(CodecType.JSON);
            }
            for (final var item : items.subList(1, items.size())) {
                writer.write((byte) ',');
                item.encodableFor(CodecType.JSON);
            }

            writer.write((byte) ']');

            return CodecType.JSON;
        };
    }
}
