package eu.arrowhead.kalix.dto;

import eu.arrowhead.kalix.dto.json.JsonWritable;

import java.nio.ByteBuffer;

/**
 * Utilities for encoding DTO classes.
 */
public class DataWriter {
    private DataWriter() {}

    /**
     * Attempts to write {@code object} encoded with {@code encoding} to
     * {@code target}.
     *
     * @param object   Object to encode and write to {@code target}.
     * @param encoding Encoding to use when writing {@code object}.
     * @param target   Byte buffer to write encoded form of {@code object} to.
     * @param <T>      Type of {@code object}.
     * @throws UnsupportedOperationException If {@code encoding} is
     *                                       {@link DataEncoding#UNSUPPORTED}.
     * @throws WriteException                If writing to {@code target} fails.
     */
    public static <T extends DataWritable> void write(
        final T object,
        final DataEncoding encoding,
        final ByteBuffer target) throws WriteException
    {
        switch (encoding) {
        case JSON:
            writeJson(object, target);
            return;

        case UNSUPPORTED:
            throw new UnsupportedOperationException("Unsupported encoding");
        }
        throw new IllegalStateException("DataEncoding that is supported has " +
            "not yet been added to this method");
    }

    private static <T extends DataWritable> void writeJson(
        final T object,
        final ByteBuffer target) throws WriteException
    {
        if (object instanceof JsonWritable) {
            ((JsonWritable) object).writeJson(target);
        }
        else {
            throw new UnsupportedOperationException("The interface type " +
                "from which the \"" + object.getClass() + "\" DTO was " +
                "generated does not include DataEncoding.JSON as argument " +
                "to its @Writable annotation; no JSON encoding routine has, " +
                "consequently, been generated for the class");
        }
    }
}
