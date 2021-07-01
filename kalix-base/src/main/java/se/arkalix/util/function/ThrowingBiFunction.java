package se.arkalix.util.function;

/**
 * A function, converting two input values into an output value. May throw
 * any {@link Throwable} while being executed.
 *
 * @param <T> Type of first value to be provided to function.
 * @param <U> Type of second value to be provided to function.
 * @param <R> Type of value returned from function.
 */
@FunctionalInterface
public interface ThrowingBiFunction<T, U, R> {
    /**
     * Executes this function.
     *
     * @param t First input value.
     * @param u Second input value.
     * @return Output value.
     * @throws Throwable Any kind of exception.
     */
    R apply(T t, U u) throws Throwable;
}
