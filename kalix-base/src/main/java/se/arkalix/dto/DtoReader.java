package se.arkalix.dto;

import se.arkalix.dto.binary.BinaryReader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilities for decoding DTO classes.
 */
public class DtoReader {
    private static final Map<Class<?>, Method> CLASS_TO_READ_JSON = new ConcurrentHashMap<>();

    private DtoReader() {}

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
     *                                       given {@link DtoEncoding} as
     *                                       argument to its
     *                                       {@code @DtoReadableAs} annotation.
     * @throws DtoReadException              If reading from {@code source}
     *                                       fails.
     * @throws NullPointerException          If {@code class_}, {@code
     *                                       encoding} or {@code source} is
     *                                       {@code null}.
     */
    public static <T extends DtoReadable> T read(
        final Class<T> class_,
        final DtoEncoding encoding,
        final BinaryReader source) throws DtoReadException
    {
        Objects.requireNonNull(class_, "Expected class_");
        Objects.requireNonNull(encoding, "Expected encoding");
        Objects.requireNonNull(source, "Expected source");

        if (encoding == DtoEncoding.JSON) {
            return readJson(class_, encoding, source);
        }
        throw new IllegalStateException("DataEncoding that is supported " +
            "has not yet been added to this method");
    }

    private static <T> T readJson(
        final Class<T> class_,
        final DtoEncoding encoding,
        final BinaryReader source) throws DtoReadException
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

            if (targetException instanceof DtoReadException) {
                throw (DtoReadException) targetException;
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

    private static RuntimeException encodingNotSupportedBy(final DtoEncoding encoding, final Class<?> class_) {
        return new UnsupportedOperationException("The interface type from " +
            "which the \"" + class_ + "\" DTO was generated does not " +
            "include DataEncoding." + encoding + " as argument to its " +
            "@DtoReadableAs annotation; no corresponding decoding routine has, " +
            "consequently, been generated for the class");
    }
}
