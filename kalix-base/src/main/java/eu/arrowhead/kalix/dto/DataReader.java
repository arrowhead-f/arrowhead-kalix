package eu.arrowhead.kalix.dto;

import eu.arrowhead.kalix.dto.binary.BinaryReader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilities for decoding DTO classes.
 */
public class DataReader {
    private static final Map<Class<?>, Method> CLASS_TO_READ_JSON = new ConcurrentHashMap<>();

    private DataReader() {}

    /**
     * Attempts to read a value encoded with {@code encoding} from
     * {@code source} byte buffer.
     *
     * @param class_   Class of value to read.
     * @param encoding Encoding decode read value with.
     * @param source   Byte buffer from which the desired value is to be read.
     * @param <T>      Type of desired value.
     * @throws UnsupportedOperationException If the DTO interface type of
     *                                       {@code t} does not include the
     *                                       given {@link DataEncoding} as
     *                                       argument to its {@code @Readable}
     *                                       annotation.
     * @throws ReadException                 If reading from {@code source}
     *                                       fails.
     */
    public static <T extends DataReadable> T read(
        final Class<T> class_,
        final DataEncoding encoding,
        final BinaryReader source) throws ReadException
    {
        if (encoding == DataEncoding.JSON) {
            return readJson(class_, encoding, source);
        }
        throw new IllegalStateException("DataEncoding that is supported " +
            "has not yet been added to this method");
    }

    private static <T> T readJson(
        final Class<T> class_,
        final DataEncoding encoding,
        final BinaryReader source) throws ReadException
    {
        final var method = CLASS_TO_READ_JSON.computeIfAbsent(class_, (ignored) -> {
            try {
                return class_.getDeclaredMethod("readJson", BinaryReader.class);
            }
            catch (final NoSuchMethodException e) {
                throw encodingNotSupportedBy(encoding, class_);
            }
        });
        try {
            return class_.cast(method.invoke(null, source));
        }
        catch (final IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
        catch (final InvocationTargetException exception) {
            final var targetException = exception.getTargetException();

            if (targetException instanceof ReadException) {
                throw (ReadException) targetException;
            }
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            }
            if (targetException instanceof Error) {
                throw (Error) targetException;
            }
            throw new RuntimeException(targetException);
        }
    }

    private static RuntimeException encodingNotSupportedBy(final DataEncoding encoding, final Class<?> class_) {
        return new UnsupportedOperationException("The interface type from " +
            "which the \"" + class_ + "\" DTO was generated does not " +
            "include DataEncoding." + encoding + " as argument to its " +
            "@Readable annotation; no corresponding decoding routine has, " +
            "consequently, been generated for the class");
    }
}
