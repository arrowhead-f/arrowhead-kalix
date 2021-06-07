package se.arkalix.codec;

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
     * @param codecType Codec to be used to encode the data written to
     *                  {@code writer}.
     * @throws CodecUnsupported     If no support exists for encoding data
     *                              using {@code codec}.
     * @throws EncodableException   If this object violates a constraint
     *                              imposed by the given codec.
     * @throws NullPointerException If {@code writer} or {@code codec} is
     *                              {@code null}.
     */
    Encodable encodableFor(CodecType codecType);
}
