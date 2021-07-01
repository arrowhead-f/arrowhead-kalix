package se.arkalix.util.function;

/**
 * A value consumer, taking two input values and executing some arbitrary
 * action. May throw any {@link Throwable} while being executed.
 *
 * @param <T> Type of first value to be provided to consumer.
 * @param <U> Type of second value to be provided to consumer.
 */
@FunctionalInterface
public interface ThrowingBiConsumer<T, U> {
    /**
     * Executes this consumer.
     *
     * @param t First input value.
     * @param u Second input value.
     * @throws Throwable Any kind of exception.
     */
    void accept(T t, U u) throws Throwable;
}
