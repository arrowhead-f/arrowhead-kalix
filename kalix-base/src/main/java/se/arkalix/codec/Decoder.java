package se.arkalix.codec;

import se.arkalix.io.buf.BufferReader;

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
     * @param reader Buffer reader.
     * @return Decoded object.
     * @throws DecoderException     If read data does not conform to the codec
     *                              of this decoder.
     * @throws NullPointerException If {@code reader} is {@code null}.
     */
    T decode(final BufferReader reader);
}
