package se.arkalix.encoding;

import se.arkalix.encoding.binary.BinaryReader;

/**
 * A functional used to decode binary data using one out of a set of different
 * encodings.
 *
 * @param <T> Type produced by successful applications of this function.
 */
@FunctionalInterface
public interface MultiDecoder<T> {
    /**
     * Decodes binary data available via {@code reader} into concrete class
     * instance using the given {@code encoding}.
     *
     * @param reader   Binary data reader.
     * @param encoding Encoding to be used to decode the data of {@code reader}.
     * @return Decoded object.
     * @throws EncodingUnsupported  If no support exists for decoding data using
     *                              {@code encoding}.
     * @throws NullPointerException If {@code reader} or {@code encoding} is
     *                              {@code null}.
     */
    T decodeUsing(BinaryReader reader, Encoding encoding);
}
