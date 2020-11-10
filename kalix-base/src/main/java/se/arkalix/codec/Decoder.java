package se.arkalix.codec;

import se.arkalix.codec.binary.BinaryReader;

/**
 * A function used to decode binary data into a concrete class instance.
 *
 * @param <T> Type produced by successful applications of this function.
 */
@FunctionalInterface
public interface Decoder<T> {
    /**
     * Decodes binary data available via {@code reader} into concrete class
     * instance.
     *
     * @param reader Binary data reader.
     * @return Decoded object.
     * @throws NullPointerException If {@code reader} is {@code null}.
     */
    T decode(final BinaryReader reader);
}
