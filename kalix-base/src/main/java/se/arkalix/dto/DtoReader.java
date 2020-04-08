package se.arkalix.dto;

import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.internal.dto.json.JsonReader;
import se.arkalix.internal.dto.json.JsonTokenBuffer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
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
     * Attempts to read one value encoded with {@code encoding} from {@code
     * source} byte reader.
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
    public static <T extends DtoReadable> T readOne(
        final Class<T> class_,
        final DtoEncoding encoding,
        final BinaryReader source) throws DtoReadException
    {
        Objects.requireNonNull(class_, "Expected class_");
        Objects.requireNonNull(encoding, "Expected encoding");
        Objects.requireNonNull(source, "Expected source");

        if (encoding == DtoEncoding.JSON) {
            return JsonReader.readOne(buffer -> invoke(class_, resolveReadJson(class_), buffer), source);
        }
        throw new IllegalStateException("DataEncoding that is supported " +
            "has not yet been added to this method");
    }

    public static <T extends DtoReadable> List<T> readMany(
        final Class<T> class_,
        final DtoEncoding encoding,
        final BinaryReader source) throws DtoReadException
    {
        Objects.requireNonNull(class_, "Expected class_");
        Objects.requireNonNull(encoding, "Expected encoding");
        Objects.requireNonNull(source, "Expected source");

        if (encoding == DtoEncoding.JSON) {
            final var method = resolveReadJson(class_);
            return JsonReader.readMany(buffer -> invoke(class_, method, buffer), source);
        }
        throw new IllegalStateException("DtoEncoding that is supported " +
            "has not yet been added to this method");
    }

    private static <T> Method resolveReadJson(final Class<T> class_) {
        return CLASS_TO_READ_JSON.computeIfAbsent(class_, ignored -> {
            try {
                return class_.getDeclaredMethod("readJson", JsonTokenBuffer.class);
            }
            catch (final NoSuchMethodException e) {
                throw jsonNotSupportedBy(class_);
            }
        });
    }

    private static <T> T invoke(
        final Class<T> returnType,
        final Method method,
        final Object arguments) throws DtoReadException
    {
        try {
            return returnType.cast(method.invoke(null, arguments));
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

    private static RuntimeException jsonNotSupportedBy(final Class<?> class_) {
        return new UnsupportedOperationException("The interface type from " +
            "which the \"" + class_ + "\" DTO was generated does not " +
            "include DtoEncoding.JSON as argument to its @DtoReadableAs " +
            "annotation; no corresponding decoding routine has, " +
            "consequently, been generated for the class");
    }
}
