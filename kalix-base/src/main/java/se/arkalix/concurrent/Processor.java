package se.arkalix.concurrent;

import se.arkalix.util.Failure;
import se.arkalix.util.Result;
import se.arkalix.util.Success;

public interface Processor<T> {
    Flow<T> flow();

    void push(T item);

    boolean isCompleted();

    default void complete(final Result<?> result) {
        if (!tryComplete(result)) {
            throw new IllegalStateException();
        }
    }

    boolean tryComplete(Result<?> result);

    default void stop(final Object value) {
        complete(Success.of(value));
    }

    default boolean tryStop(final Object value) {
        return tryComplete(Success.of(value));
    }

    default void fail(final Throwable exception) {
        complete(Failure.of(exception));
    }

    default boolean tryFail(final Throwable exception) {
        return tryComplete(Failure.of(exception));
    }
}
