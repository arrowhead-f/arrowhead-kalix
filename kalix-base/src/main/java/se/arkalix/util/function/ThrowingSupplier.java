package se.arkalix.util.function;


/**
 * A supplier, producing an output value. May throw any {@link Throwable} while
 * being executed.
 *
 * @param <T> Type of value returned from supplier.
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {
    /**
     * Executes this supplier for its result.
     *
     * @return Output value.
     * @throws Throwable Any kind of exception.
     */
    T get() throws Throwable;
}
