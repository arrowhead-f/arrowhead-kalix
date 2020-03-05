package eu.arrowhead.kalix.util.concurrent;

import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.function.ThrowingFunction;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * A {@code Future} that always fails with a predetermined error.
 *
 * @param <V> Type of value that would have been included if successful.
 */
class FutureFailure<V> implements FutureProgress<V> {
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
        Objects.requireNonNull(consumer, "Expected consumer");
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
    public FutureProgress<V> onProgress(final Listener listener) {
        Objects.requireNonNull(listener, "Expected listener");
        return this;
    }

    @Override
    public <U> Future<U> map(final ThrowingFunction<? super V, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        return new FutureFailure<>(error);
    }

    @Override
    public Future<V> mapCatch(final ThrowingFunction<Throwable, ? extends V> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        try {
            return new FutureSuccess<>(mapper.apply(error));
        }
        catch (final Throwable throwable) {
            return new FutureFailure<>(throwable);
        }
    }

    @Override
    public Future<V> mapError(final ThrowingFunction<Throwable, Throwable> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        Throwable err;
        try {
            err = mapper.apply(error);
        }
        catch (final Throwable throwable) {
            err = throwable;

        }
        return new FutureFailure<>(err);
    }

    @Override
    public <U> Future<U> flatMap(final ThrowingFunction<? super V, ? extends Future<U>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        return new FutureFailure<>(error);
    }

    @Override
    public Future<V> flatMapCatch(final ThrowingFunction<Throwable, ? extends Future<V>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        try {
            return mapper.apply(error);
        }
        catch (final Throwable throwable) {
            return new FutureFailure<>(throwable);
        }
    }

    @Override
    public Future<V> flatMapError(final ThrowingFunction<Throwable, ? extends Future<? extends Throwable>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        final var cancelTarget = new AtomicReference<Future<?>>(this);
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                try {
                    final var future1 = mapper.apply(error);
                    future1.onResult(result -> consumer.accept(Result.failure(result.isSuccess()
                        ? result.value()
                        : result.error())));
                    cancelTarget.set(future1);
                }
                catch (final Throwable error) {
                    consumer.accept(Result.failure(error));
                }
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                final var target = cancelTarget.getAndSet(null);
                if (target != null) {
                    target.cancel(mayInterruptIfRunning);
                }
            }
        };
    }
}
