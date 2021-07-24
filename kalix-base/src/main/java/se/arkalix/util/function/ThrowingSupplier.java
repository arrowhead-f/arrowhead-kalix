package se.arkalix.util.function;


/**
 * A callable, producing an output value. May throw any {@link Throwable} while
 * being executed.
 *
 * @param <T> Type of value returned from callable.
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {
    /**
     * Executes this callable for its result.
     *
     * @return Output value.
     * @throws Throwable Any kind of exception.
     */
    T get() throws Throwable;
}
