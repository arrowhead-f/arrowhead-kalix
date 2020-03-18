package eu.arrowhead.kalix.util.function;

/**
 * A function, converting an input value into an output value. May throw
 * any {@link Exception} while being executed.
 *
 * @param <V> Type of value to be provided to function.
 * @param <U> Type of value returned from function.
 */
@FunctionalInterface
public interface ThrowingFunction<V, U> {
    /**
     * Executes this function.
     *
     * @param v Input value.
     * @return Output value.
     * @throws Exception Any kind of exception.
     */
    U apply(V v) throws Throwable;
}
