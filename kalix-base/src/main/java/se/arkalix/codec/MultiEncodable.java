package se.arkalix.codec;

import se.arkalix.codec.binary.BinaryWriter;

/**
 * A function used to encode an implicit class instance into binary data using
 * one out of a set of different codecs.
 */
@FunctionalInterface
public interface MultiEncodable {
    /**
     * Encodes implicit class instance and writes it to {@code writer}. The
     * given {@code codec} is used if supported.
     *
     * @param writer   Binary data writer.
     * @param codecType Codec to be used to encode the data wrtten to
     *                 {@code writer}.
     * @throws CodecUnsupported  If no support exists for codec data using
     *                              {@code codec}.
     * @throws NullPointerException If {@code writer} or {@code codec} is
     *                              {@code null}.
     */
    void encode(BinaryWriter writer, CodecType codecType);
}
