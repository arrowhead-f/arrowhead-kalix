package eu.arrowhead.kalix.dto;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilities for decoding DTO classes.
 */
public class DataReader {
    private static final Map<Class<?>, Method> JSON_METHODS = new ConcurrentHashMap<>();

    private DataReader() {}

    public static <T extends DataReadable> T read(
        final Class<T> class_,
        final DataEncoding encoding,
        final ByteBuffer source) throws ReadException
    {
        try {

            switch (encoding) {
            case JSON:
                return class_.cast(getReadJsonMethod(class_).invoke(null, source));

            case UNSUPPORTED:
                throw new UnsupportedOperationException("Unsupported encoding");
            }
            throw new IllegalStateException("DataEncoding that is supported " +
                "has not yet been added to this method");

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

    public static Method getReadJsonMethod(final Class<? extends DataReadable> class_) {
        return JSON_METHODS.computeIfAbsent(class_, (class_0) -> {
            try {
                return class_.getDeclaredMethod("readJson", ByteBuffer.class);
            }
            catch (final NoSuchMethodException e) {
                throw new UnsupportedOperationException("The interface " +
                    "type from which the \"" + class_ + "\" DTO was " +
                    "generated does not include DataEncoding.JSON as " +
                    "argument to its @Readable annotation; no JSON " +
                    "decoding routine has, consequently, been generated " +
                    "for the class");
            }
        });
    }
}
