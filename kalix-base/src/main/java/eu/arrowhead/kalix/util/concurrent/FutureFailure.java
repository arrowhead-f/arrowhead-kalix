package eu.arrowhead.kalix.util.concurrent;

import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.function.ThrowingFunction;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A {@code Future} that always fails with a predetermined error.
 *
 * @param <V> Type of value that would have been included if successful.
 */
class FutureFailure<V> implements Future<V> {
    private final Throwable error;
    private boolean isDone = false;

    /**
     * Creates new failing {@link Future}.
     *
     * @param error Error to include in {@code Future}.
     * @throws NullPointerException If {@code error} is {@code null}.
     */
    public FutureFailure(final Throwable error) {
        this.error = Objects.requireNonNull(error);
    }

    @Override
    public void onResult(final Consumer<Result<V>> consumer) {
        if (!isDone) {
            consumer.accept(Result.failure(error));
            isDone = true;
        }
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        isDone = true;
    }

    @Override
    public <U> Future<U> map(final ThrowingFunction<? super V, ? extends U> mapper) {
        return new FutureFailure<>(error);
    }

    @Override
    public <U> Future<U> flatMap(final ThrowingFunction<? super V, ? extends Future<U>> mapper) {
        return new FutureFailure<>(error);
    }
}
