package eu.arrowhead.kalix.concurrent;

import java.util.function.Consumer;

/**
 * A {@code Future} that always succeeds with a predetermined value.
 *
 * @param <V> Type of value.
 */
public class FutureSuccess<V> implements Future<V> {
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
    public void onResult(final Consumer<FutureResult<V>> consumer) {
        if (!isDone) {
            consumer.accept(FutureResult.success(value));
            isDone = true;
        }
    }

    @Override
    public void cancel() {
        isDone = true;
    }

    @Override
    public <U> Future<U> map(final Mapper<? super V, ? extends U> mapper) {
        try {
            return new FutureSuccess<>(mapper.apply(value));
        }
        catch (final Throwable error) {
            return new FutureFailure<>(error);
        }
    }

    @Override
    public <U> Future<U> flatMap(final Mapper<? super V, ? extends Future<U>> mapper) {
        try {
            return mapper.apply(value);
        }
        catch (final Throwable error) {
            return new FutureFailure<>(error);
        }
    }
}
