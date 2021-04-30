package se.arkalix.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A wrapper allowing for lists of decodable objects to be decoded together.
 * <p>
 * Not all codecs support anonymous lists, and not all codecs that do are
 * supported by this class. Use {@link #supportedEncodings()} to get a set of
 * all supported codecs. As a rule, all codecs supported by the Kalix DTO
 * package should be supported here, unless the codec in question does not have
 * an anonymous list type, such as in the case of XML.
 *
 * @param <T> Decoded list item type.
 */
public class MultiDecoderForLists<T> implements MultiDecoder<List<T>> {
    private static final Set<CodecType> supportedEncodings = Set.of(CodecType.JSON);

    private final MultiDecoder<T> decoder;

    /**
     * Gets set of codecs supported by instances of this class.
     *
     * @return Set of supported codecs.
     */
    public static Set<CodecType> supportedEncodings() {
        return supportedEncodings;
    }

    private MultiDecoderForLists(final MultiDecoder<T> decoder) {
        this.decoder = Objects.requireNonNull(decoder, "decoder");
    }

    /**
     * Creates new list decoder, wrapping given item decoder.
     *
     * @param decoder Decoder useful for decoding individual items.
     * @param <T>     Type of items.
     * @return New {@link MultiEncodableForLists}.
     */
    public static <T> MultiDecoderForLists<T> of(final MultiDecoder<T> decoder) {
        return new MultiDecoderForLists<>(decoder);
    }

    @Override
    public Decoder<List<T>> decoderFor(final CodecType codecType) {
        if (codecType == CodecType.JSON) {
            return decoderForJson();
        }
        else {
            throw new CodecUnsupported(codecType);
        }
    }

    /**
     * Gets {@link Decoder} supporting JSON arrays.
     */
    public Decoder<List<T>> decoderForJson() {
        return reader -> {
            if (reader == null) {
                throw new NullPointerException("reader");
            }

            byte b;

            do {
                if (reader.readableBytes() <= 0) {
                    throw new DecoderReadUnexpectedToken(
                        CodecType.JSON,
                        reader,
                        "",
                        reader.readOffset(),
                        "cannot decode empty string");
                }
                b = reader.readByte();
            } while (b == ' ' || b == '\t' || b == '\r' || b == '\n');

            if (b != '[') {
                throw new DecoderReadUnexpectedToken(
                    CodecType.JSON,
                    reader,
                    Character.toString(b),
                    reader.readOffset(),
                    "expected '['");
            }

            final var list = new ArrayList<T>();

            loop:
            while (true) {
                do {
                    if (reader.readableBytes() <= 0) {
                        throw new DecoderReadUnexpectedToken(
                            CodecType.JSON,
                            reader,
                            "",
                            reader.readOffset(),
                            "array ended unexpectedly");
                    }
                    b = reader.readByte();
                } while (b == ' ' || b == '\t' || b == '\r' || b == '\n');

                list.add(decoder.decoderFor(CodecType.JSON).decode(reader));

                do {
                    if (reader.readableBytes() <= 0) {
                        throw new DecoderReadUnexpectedToken(
                            CodecType.JSON,
                            reader,
                            "",
                            reader.readOffset(),
                            "array ended unexpectedly");
                    }
                    b = reader.readByte();
                } while (b == ' ' || b == '\t' || b == '\r' || b == '\n');

                switch (b) {
                case ',':
                    continue;

                case ']':
                    break loop;

                default:
                    throw new DecoderReadUnexpectedToken(
                        CodecType.JSON,
                        reader,
                        Character.toString(b),
                        reader.readOffset(),
                        "expected ',' or ']'");
                }
            }

            return list;
        };
    }
}
