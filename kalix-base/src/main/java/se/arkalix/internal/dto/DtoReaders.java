package se.arkalix.internal.dto;

import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.binary.BinaryReader;
import se.arkalix.util.annotation.Internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Internal
public class DtoReaders {
    private DtoReaders() {}

    public static <T> T invokeReadMethod(final Class<T> returnType, final Method method, final Object source)
        throws DtoReadException
    {
        try {
            return returnType.cast(method.invoke(null, source));
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
}
