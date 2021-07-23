package se.arkalix.codec;

import se.arkalix.io.buffer.BufferWriter;

/**
 * A function used to encode an implicit class instance into binary data.
 */
@FunctionalInterface
public interface Encodable {
    /**
     * Writes this object in encoded form to {@code writer}.
     *
     * @param writer Buffer writer.
     * @return Codec applied by this encodable, or {@link CodecType#NONE} if
     * not relevant.
     * @throws NullPointerException If {@code writer} is {@code null}.
     */
    CodecType encode(BufferWriter writer);
}
