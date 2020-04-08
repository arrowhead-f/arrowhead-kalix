package se.arkalix.dto;

import se.arkalix.dto.binary.BinaryWriter;
import se.arkalix.dto.json.JsonWritable;
import se.arkalix.internal.dto.json.JsonWriter;

import java.util.List;

/**
 * Utilities for encoding DTO classes.
 */
public class DtoWriter {
    private DtoWriter() {}

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
     *                                       given {@link DtoEncoding} as
     *                                       argument to its
     *                                       {@code @DtoWritableAs} annotation.
     * @throws DtoWriteException             If writing to {@code target} fails.
     */
    public static <T extends DtoWritable> void writeOne(
        final T t,
        final DtoEncoding encoding,
        final BinaryWriter target) throws DtoWriteException
    {
        if (encoding == DtoEncoding.JSON) {
            if (t instanceof JsonWritable) {
                ((JsonWritable) t).writeJson(target);
                return;
            }
            throw jsonNotSupportedFor(t);
        }
        throw new IllegalStateException("DtoEncoding that is supported has " +
            "not yet been added to this method");
    }

    @SuppressWarnings("unchecked")
    public static <T extends DtoWritable> void writeMany(
        final List<T> ts,
        final DtoEncoding encoding,
        final BinaryWriter target) throws DtoWriteException
    {
        if (encoding == DtoEncoding.JSON) {
            if (!ts.isEmpty() && !(ts.get(0) instanceof JsonWritable)) {
                throw jsonNotSupportedFor(ts.get(0));
            }
            JsonWriter.writeMany((List<JsonWritable>) ts, target);
            return;
        }
        throw new IllegalStateException("DtoEncoding that is supported has " +
            "not yet been added to this method");
    }

    private static RuntimeException jsonNotSupportedFor(final Object object) {
        return new UnsupportedOperationException("The interface type from " +
            "which the \"" + object.getClass() + "\" DTO was generated does " +
            "not include DtoEncoding.JSON as argument to its @DtoWritableAs " +
            "annotation; no corresponding encoding routine has, " +
            "consequently, been generated for the class");
    }
}
