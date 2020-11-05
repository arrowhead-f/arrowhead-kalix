package se.arkalix.encoding;

import se.arkalix.encoding.binary.BinaryWriter;

import java.util.Optional;

/**
 * A function used to encode an implicit class instance into binary data.
 */
@FunctionalInterface
public interface Encodable {
    /**
     * Encodes implicit class instance and writes it to {@code writer}.
     *
     * @param writer Binary data writer.
     * @return Encoding applied by this encoder, or {@link Encoding#NONE} if
     * not relevant.
     * @throws NullPointerException If {@code writer} is {@code null}.
     */
    Encoding encode(BinaryWriter writer);
}
