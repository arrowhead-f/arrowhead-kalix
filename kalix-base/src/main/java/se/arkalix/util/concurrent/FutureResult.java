package se.arkalix.util.concurrent;

import se.arkalix.util.Result;
import se.arkalix.util.function.ThrowingConsumer;
import se.arkalix.util.function.ThrowingFunction;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A {@code Future} that always completes with a predetermined result.
 *
 * @param <V> Type of value that is included if the result is successful.
 */
class FutureResult<V> implements Future<V> {
    private final Result<V> result;

    /**
     * Creates new {@link Future} that always completes with the given
     * {@code result}.
     *
     * @param result Result to include in {@code Future}.
     */
    public FutureResult(final Result<V> result) {
        this.result = result;
    }

    @Override
    public void onResult(final Consumer<Result<V>> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        consumer.accept(result);
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        // Does nothing.
    }

    @Override
    public void onFailure(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        if (result.isFailure()) {
            consumer.accept(result.fault());
        }
    }

    @Override
    public Future<V> ifSuccess(final ThrowingConsumer<V> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        if (result.isSuccess()) {
            try {
                consumer.accept(result.value());
            }
            catch (final Throwable throwable) {
                return Future.failure(throwable);
            }
        }
        return this;
    }

    @Override
    public <T extends Throwable> Future<V> ifFailure(final Class<T> class_, final ThrowingConsumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        if (result.isFailure()) {
            final var fault = result.fault();
            if (class_.isAssignableFrom(fault.getClass())) {
                try {
                    consumer.accept(class_.cast(fault));
                }
                catch (final Throwable throwable) {
                    throwable.addSuppressed(fault);
                    return Future.failure(throwable);
                }
            }
        }
        return this;
    }

    @Override
    public Future<V> always(final ThrowingConsumer<Result<V>> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        try {
            consumer.accept(result);
        }
        catch (final Throwable throwable) {
            if (result.isFailure()) {
                throwable.addSuppressed(result.fault());
            }
            return Future.failure(throwable);
        }
        return this;
    }

    @Override
    public <U> Future<U> map(final ThrowingFunction<? super V, U> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        Throwable fault;
        if (result.isSuccess()) {
            try {
                return Future.success(mapper.apply(result.value()));
            }
            catch (final Throwable throwable) {
                fault = throwable;
            }
        }
        else {
            fault = result.fault();
        }
        return Future.failure(fault);
    }

    @Override
    public <U extends Throwable> Future<V> mapCatch(
        final Class<U> class_,
        final ThrowingFunction<U, ? extends V> mapper)
    {
        Objects.requireNonNull(class_, "class_");
        Objects.requireNonNull(mapper, "mapper");
        if (result.isSuccess()) {
            return this;
        }
        var fault = result.fault();
        if (class_.isAssignableFrom(fault.getClass())) {
            try {
                return Future.success(mapper.apply(class_.cast(fault)));
            }
            catch (final Throwable throwable) {
                fault = throwable;
            }
        }
        return Future.failure(fault);
    }

    @Override
    public <T extends Throwable> Future<V> mapFault(
        final Class<T> class_,
        final ThrowingFunction<Throwable, Throwable> mapper)
    {
        Objects.requireNonNull(class_, "class_");
        Objects.requireNonNull(mapper, "mapper");
        if (result.isSuccess()) {
            return this;
        }
        var fault = result.fault();
        if (class_.isAssignableFrom(fault.getClass())) {
            try {
                fault = mapper.apply(fault);
            }
            catch (final Throwable throwable) {
                fault = throwable;
            }
        }
        return Future.failure(fault);
    }

    @Override
    public <U> Future<U> mapResult(final ThrowingFunction<Result<V>, Result<U>> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        try {
            return Future.of(mapper.apply(result));
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public <U> Future<U> mapThrow(final ThrowingFunction<? super V, Throwable> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        Throwable fault;
        if (result.isSuccess()) {
            try {
                fault = mapper.apply(result.value());
            }
            catch (final Throwable throwable) {
                fault = throwable;
            }
        }
        else {
            fault = result.fault();
        }
        return Future.failure(fault);
    }

    @Override
    public <U> Future<U> flatMap(final ThrowingFunction<? super V, ? extends Future<U>> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        Throwable fault;
        if (result.isSuccess()) {
            try {
                return mapper.apply(result.value());
            }
            catch (final Throwable throwable) {
                fault = throwable;
            }
        }
        else {
            fault = result.fault();
        }
        return Future.failure(fault);
    }

    @Override
    public <U extends Throwable> Future<V> flatMapCatch(final Class<U> class_, final ThrowingFunction<U, ? extends Future<V>> mapper) {
        Objects.requireNonNull(class_, "class_");
        Objects.requireNonNull(mapper, "mapper");
        if (result.isSuccess()) {
            return this;
        }
        var fault = result.fault();
        if (class_.isAssignableFrom(fault.getClass())) {
            try {
                return mapper.apply(class_.cast(fault));
            }
            catch (final Throwable throwable) {
                fault = throwable;
            }
        }
        return Future.failure(fault);
    }

    @Override
    public <T extends Throwable> Future<V> flatMapFault(
        final Class<T> class_,
        final ThrowingFunction<Throwable, ? extends Future<Throwable>> mapper)
    {
        Objects.requireNonNull(class_, "class_");
        Objects.requireNonNull(mapper, "mapper");
        if (result.isSuccess()) {
            return this;
        }
        return new Future<>() {
            private Future<?> cancelTarget = null;
            private boolean isCancelled = false;

            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                if (isCancelled) {
                    return;
                }
                var fault = result.fault();
                if (class_.isAssignableFrom(fault.getClass())) {
                    try {
                        final var future1 = mapper.apply(fault);
                        future1.onResult(result -> consumer.accept(Result.failure(result.isSuccess()
                            ? result.value()
                            : result.fault())));
                        cancelTarget = future1;
                        return;
                    }
                    catch (final Throwable throwable) {
                        fault = throwable;
                    }
                }
                consumer.accept(Result.failure(fault));
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
    public <U> Future<U> flatMapResult(final ThrowingFunction<Result<V>, ? extends Future<U>> mapper) {
        try {
            return mapper.apply(result);
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public Future<V> flatMapThrow(final ThrowingFunction<V, ? extends Future<? extends Throwable>> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        if (result.isFailure()) {
            return this;
        }
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
                    final var future = mapper.apply(result.value());
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
        Objects.requireNonNull(value, "value");
        return result.isSuccess()
            ? Future.success(value)
            : Future.failure(result.fault());
    }

    @Override
    public <U> Future<U> fail(final Throwable throwable) {
        Objects.requireNonNull(throwable, "throwable");
        if (result.isFailure()) {
            throwable.addSuppressed(result.fault());
        }
        return Future.failure(throwable);
    }

    @Override
    public Future<V> delay(final Duration duration) {
        Objects.requireNonNull(duration, "duration");
        return new Future<>() {
            private Future<?> cancelTarget = null;

            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                cancelTarget = Schedulers.fixed()
                    .schedule(duration, () -> consumer.accept(result));
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
        Objects.requireNonNull(baseline, "baseline");
        return new Future<>() {
            private Future<?> cancelTarget = null;

            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                final var duration = Duration.between(baseline, Instant.now());
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
        return result.valueOrThrow();
    }

    @Override
    public V await(final Duration timeout) {
        return result.valueOrThrow();
    }

    @Override
    public String toString() {
        return "Future{" +
            (result.isSuccess()
                ? "value=" + result.value()
                : "fault=" + result.fault()) +
            '}';
    }
}
