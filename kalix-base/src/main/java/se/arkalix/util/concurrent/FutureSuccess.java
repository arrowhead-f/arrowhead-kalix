package se.arkalix.util.concurrent;

import se.arkalix.util.Result;
import se.arkalix.util.function.ThrowingConsumer;
import se.arkalix.util.function.ThrowingFunction;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A {@code Future} that always succeeds with a predetermined value.
 *
 * @param <V> Type of value.
 */
class FutureSuccess<V> implements FutureProgress<V> {
    static final FutureSuccess<?> NULL = new FutureSuccess<>(null);

    private final V value;

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
        Objects.requireNonNull(consumer, "consumer");
        consumer.accept(Result.success(value));
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        // Does nothing.
    }

    @Override
    public void onFailure(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer);
        // Does nothing.
    }

    @Override
    public FutureProgress<V> addProgressListener(final Listener listener) {
        Objects.requireNonNull(listener, "listener");
        // Does nothing.
        return this;
    }

    @Override
    public Future<V> ifSuccess(final ThrowingConsumer<V> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        try {
            consumer.accept(value);
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
        return this;
    }

    @Override
    public <T extends Throwable> Future<V> ifFailure(final Class<T> class_, final ThrowingConsumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        return this;
    }

    @Override
    public Future<V> always(final ThrowingConsumer<Result<V>> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        try {
            consumer.accept(Result.success(value));
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
        return this;
    }

    @Override
    public <U> Future<U> map(final ThrowingFunction<? super V, U> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        try {
            return Future.success(mapper.apply(value));
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public <U extends Throwable> Future<V> mapCatch(
        final Class<U> class_,
        final ThrowingFunction<U, ? extends V> mapper)
    {
        Objects.requireNonNull(class_, "class_");
        Objects.requireNonNull(mapper, "mapper");
        return this;
    }

    @Override
    public <T extends Throwable> Future<V> mapFault(
        final Class<T> class_,
        final ThrowingFunction<Throwable, Throwable> mapper)
    {
        Objects.requireNonNull(class_, "class_");
        Objects.requireNonNull(mapper, "Expected mapper");
        return this;
    }

    @Override
    public <U> Future<U> mapResult(final ThrowingFunction<Result<V>, Result<U>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        try {
            return new FutureResult<>(mapper.apply(Result.success(value)));
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public <U> Future<U> mapThrow(final ThrowingFunction<? super V, Throwable> mapper) {
        Throwable fault;
        try {
            fault = mapper.apply(value);
        }
        catch (final Throwable throwable) {
            fault = throwable;
        }
        return Future.failure(fault);
    }

    @Override
    public <U> Future<U> flatMap(final ThrowingFunction<? super V, ? extends Future<U>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        try {
            return mapper.apply(value);
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public <U extends Throwable> Future<V> flatMapCatch(
        final Class<U> class_,
        final ThrowingFunction<U, ? extends Future<V>> mapper)
    {
        Objects.requireNonNull(class_, "Expected class_");
        Objects.requireNonNull(mapper, "Expected mapper");
        return this;
    }

    @Override
    public <T extends Throwable> Future<V> flatMapFault(
        final Class<T> class_,
        final ThrowingFunction<Throwable, ? extends Future<Throwable>> mapper)
    {
        Objects.requireNonNull(class_, "Expected class_");
        Objects.requireNonNull(mapper, "Expected mapper");
        return this;
    }

    @Override
    public <U> Future<U> flatMapResult(final ThrowingFunction<Result<V>, ? extends Future<U>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        try {
            return mapper.apply(Result.success(value));
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public Future<V> flatMapThrow(final ThrowingFunction<V, ? extends Future<? extends Throwable>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        final var self = this;
        return new Future<>() {
            private Future<?> cancelTarget = self;
            private boolean isCancelled = false;

            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                if (isCancelled) {
                    return;
                }
                try {
                    final var future = mapper.apply(value);
                    cancelTarget = future;
                    future.onResult(result -> consumer.accept(Result.failure(result.isSuccess()
                        ? result.value()
                        : result.fault())));
                }
                catch (final Throwable throwable) {
                    consumer.accept(Result.failure(throwable));
                }
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                if (cancelTarget != null) {
                    cancelTarget.cancel(mayInterruptIfRunning);
                    cancelTarget = null;
                }
                isCancelled = true;
            }
        };
    }

    @Override
    public <U> Future<U> pass(final U value) {
        Objects.requireNonNull(value, "Expected value");
        return Future.success(value);
    }

    @Override
    public <U> Future<U> fail(final Throwable throwable) {
        Objects.requireNonNull(throwable, "Expected throwable");
        return Future.failure(throwable);
    }

    @Override
    public Future<V> delay(final Duration duration) {
        Objects.requireNonNull(duration, "Expected duration");
        return new Future<>() {
            private Future<?> cancelTarget = null;

            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                cancelTarget = Schedulers.fixed()
                    .schedule(duration, () -> consumer.accept(Result.success(value)));
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                if (cancelTarget != null) {
                    cancelTarget.cancel(mayInterruptIfRunning);
                    cancelTarget = null;
                }
            }
        };
    }

    @Override
    public Future<V> delayUntil(final Instant baseline) {
        Objects.requireNonNull(baseline, "Expected baseline");
        return new Future<>() {
            private Future<?> cancelTarget = null;

            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                final var duration = Duration.between(baseline, Instant.now());
                final var result = Result.success(value);
                if (duration.isNegative() || duration.isZero()) {
                    consumer.accept(result);
                }
                else {
                    cancelTarget = Schedulers.fixed()
                        .schedule(duration, () -> consumer.accept(result));
                }
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                if (cancelTarget != null) {
                    cancelTarget.cancel(mayInterruptIfRunning);
                    cancelTarget = null;
                }
            }
        };
    }

    @Override
    public V await() {
        return value;
    }

    @Override
    public V await(final Duration timeout) {
        return value;
    }

    @Override
    public String toString() {
        return "Future{value=" + value + '}';
    }
}
