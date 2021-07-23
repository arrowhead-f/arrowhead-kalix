package se.arkalix.concurrent;

import se.arkalix.util.Failure;
import se.arkalix.util.Result;
import se.arkalix.util.Success;

import java.util.Objects;

public interface Promise<T> {
    Future<T> future();

    boolean isCompleted();

    default void complete(final Result<T> result) {
        if (!tryComplete(result)) {
            throw new IllegalStateException();
        }
    }

    default void complete(final Future<T> future) {
        Objects.requireNonNull(future)
            .onCompletion(this::complete);
    }

    boolean tryComplete(Result<T> result);

    default void fulfill(final T value) {
        complete(Success.of(value));
    }

    default boolean tryFulfill(final T value) {
        return tryComplete(Success.of(value));
    }

    default void fail(final Throwable exception) {
        complete(Failure.of(exception));
    }

    default boolean tryFail(final Throwable exception) {
        return tryComplete(Failure.of(exception));
    }
}
