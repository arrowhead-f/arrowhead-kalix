package eu.arrowhead.kalix.dto;

import eu.arrowhead.kalix.dto.binary.BinaryWriter;
import eu.arrowhead.kalix.dto.json.JsonWritable;

import java.nio.ByteBuffer;

/**
 * Utilities for encoding DTO classes.
 */
public class DataWriter {
    private DataWriter() {}

    /**
     * Attempts to write {@code t} encoded with {@code encoding} to
     * {@code target} byte buffer.
     *
     * @param t        Object to encode and write to {@code target}.
     * @param encoding Encoding to use when writing {@code t}.
     * @param target   Byte buffer to write encoded form of {@code t} to.
     * @param <T>      Type of {@code t}.
     * @throws UnsupportedOperationException If the DTO interface type of
     *                                       {@code t} does not include the
     *                                       given {@link DataEncoding} as
     *                                       argument to its {@code @Writable}
     *                                       annotation.
     * @throws WriteException                If writing to {@code target} fails.
     */
    public static <T extends DataWritable> void write(final T t, final DataEncoding encoding, final BinaryWriter target)
        throws WriteException
    {
        if (encoding == DataEncoding.JSON) {
            if (t instanceof JsonWritable) {
                ((JsonWritable) t).writeJson(target);
                return;
            }
            throw encodingNotSupportedFor(encoding, t);
        }
        throw new IllegalStateException("DataEncoding that is supported has " +
            "not yet been added to this method");
    }

    private static RuntimeException encodingNotSupportedFor(final DataEncoding encoding, final Object object) {
        return new UnsupportedOperationException("The interface type from " +
            "which the \"" + object.getClass() + "\" DTO was generated does " +
            "not include DataEncoding." + encoding + " as argument to its " +
            "@Writable annotation; no corresponding encoding routine has, " +
            "consequently, been generated for the class");
    }
}
