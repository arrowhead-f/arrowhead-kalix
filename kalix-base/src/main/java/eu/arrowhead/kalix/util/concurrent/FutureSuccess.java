package eu.arrowhead.kalix.util.concurrent;

import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.function.ThrowingFunction;

import java.util.function.Consumer;

/**
 * A {@code Future} that always succeeds with a predetermined value.
 *
 * @param <V> Type of value.
 */
class FutureSuccess<V> implements Future<V> {
    private final V value;
    private boolean isDone = false;

    /**
     * Creates new successful {@link Future}.
     *
     * @param value Value to include in {@code Future}.
     */
    public FutureSuccess(final V value) {
        this.value = value;
    }

    @Override
    public void onResult(final Consumer<Result<V>> consumer) {
        if (!isDone) {
            consumer.accept(Result.success(value));
            isDone = true;
        }
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        isDone = true;
    }

    @Override
    public <U> Future<U> map(final ThrowingFunction<? super V, ? extends U> mapper) {
        try {
            return Future.success(mapper.apply(value));
        }
        catch (final Throwable error) {
            return Future.failure(error);
        }
    }

    @Override
    public <U> Future<U> flatMap(final ThrowingFunction<? super V, ? extends Future<U>> mapper) {
        try {
            return mapper.apply(value);
        }
        catch (final Throwable error) {
            return Future.failure(error);
        }
    }
}
