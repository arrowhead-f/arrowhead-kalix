package se.arkalix.codec;

import se.arkalix.codec.binary.BinaryReader;

/**
 * A functional used to decode binary data using one out of a set of different
 * codecs.
 *
 * @param <T> Type produced by successful applications of this function.
 */
@FunctionalInterface
public interface MultiDecoder<T> {
    /**
     * Decodes binary data available via {@code reader} into concrete class
     * instance using the given {@code codec}.
     *
     * @param reader   Binary data reader.
     * @param codecType Codec to be used to decode the data of {@code reader}.
     * @return Decoded object.
     * @throws CodecUnsupported  If no support exists for decoding data using
     *                              {@code codec}.
     * @throws NullPointerException If {@code reader} or {@code codec} is
     *                              {@code null}.
     */
    T decode(BinaryReader reader, CodecType codecType);
}
