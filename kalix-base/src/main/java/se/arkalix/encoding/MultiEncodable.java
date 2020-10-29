package se.arkalix.encoding;

import se.arkalix.encoding.binary.BinaryWriter;

/**
 * A function used to encode an implicit class instance into binary data using
 * one out of a set of different encodings.
 */
@FunctionalInterface
public interface MultiEncodable {
    /**
     * Encodes implicit class instance and writes it to {@code writer}. The
     * given {@code encoding} is used if supported.
     *
     * @param writer   Binary data writer.
     * @param encoding Encoding to be used to encode the data wrtten to
     *                 {@code writer}.
     * @throws EncodingUnsupported  If no support exists for encoding data using
     *                              {@code encoding}.
     * @throws NullPointerException If {@code writer} or {@code encoding} is
     *                              {@code null}.
     */
    void encodeUsing(BinaryWriter writer, Encoding encoding);
}
