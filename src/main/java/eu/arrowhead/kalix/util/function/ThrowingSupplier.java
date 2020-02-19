package eu.arrowhead.kalix.util.function;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Throwable> {
    /**
     * Gets a result.
     *
     * @return a result
     */
    T get() throws E;
}
