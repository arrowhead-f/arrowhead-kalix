package se.arkalix.util.function;

/**
 * A value consumer, taking one input value and executing some arbitrary
 * action. May throw any {@link Exception} while being executed.
 *
 * @param <T> Type of value to be provided to consumer.
 */
@FunctionalInterface
public interface ThrowingConsumer<T> {
    /**
     * Executes this consumer.
     *
     * @param t Input value.
     * @throws Exception Any kind of exception.
     */
    void accept(T t) throws Throwable;
}
