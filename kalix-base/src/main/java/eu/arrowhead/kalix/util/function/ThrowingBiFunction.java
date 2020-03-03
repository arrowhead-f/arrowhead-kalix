package eu.arrowhead.kalix.util.function;

/**
 * A function, converting two input values into an output value. May throw
 * any {@link Throwable} while being executed.
 *
 * @param <T> Type of first value to be provided to mapper.
 * @param <U> Type of second value to be provided to mapper.
 * @param <R> Type of value returned from mapper.
 */
@FunctionalInterface
public interface ThrowingBiFunction<T, U, R> {
    /**
     * Provides mapper with a value and receives its output value.
     *
     * @param t First value to provide.
     * @param u Second value to provide.
     * @return Output value.
     * @throws Throwable Any kind of exception.
     */
    R apply(T t, U u) throws Throwable;
}
