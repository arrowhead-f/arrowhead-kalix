package eu.arrowhead.kalix.util.function;

/**
 * A function, converting an input value into an output value. May throw
 * any {@link Exception} while being executed.
 *
 * @param <V> Type of value to be provided to mapper.
 * @param <U> Type of value returned from mapper.
 */
@FunctionalInterface
public interface ThrowingFunction<V, U> {
    /**
     * Provides mapper with a value and receives its output value.
     *
     * @param v Value to provide.
     * @return Output value.
     * @throws Exception Any kind of exception.
     */
    U apply(V v) throws Exception;
}
