package se.arkalix.util.function;

/**
 * A function, converting an input value into an output value. May throw
 * any {@link Throwable} while being executed.
 *
 * @param <T> Type of value to be provided to function.
 * @param <U> Type of value returned from function.
 */
@FunctionalInterface
public interface ThrowingFunction<T, U> {
    /**
     * Executes this function.
     *
     * @param t Input value.
     * @return Output value.
     * @throws Throwable Any kind of exception.
     */
    U apply(T t) throws Throwable;
}
