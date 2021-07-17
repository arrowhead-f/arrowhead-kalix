package se.arkalix.util.concurrent._internal;

import se.arkalix.util.concurrent.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Schedulers;
import se.arkalix.util.function.ThrowingConsumer;
import se.arkalix.util.function.ThrowingFunction;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;

@Internal
public class CertainFailure<V> implements Future<V> {
    private final Throwable fault;

    public CertainFailure(final Throwable fault) {
        this.fault = Objects.requireNonNull(fault);
    }

    @Override
    public void await(final Consumer<Result<V>> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        consumer.accept(Result.failure(fault));
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        // Does nothing.
    }

    @Override
    public void consumeIfFault(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer);
        consumer.accept(fault);
    }

    @Override
    public Future<V> andIfSuccess(final ThrowingConsumer<V> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        // Does nothing.
        return this;
    }

    @Override
    public <T extends Throwable> Future<V> andIfFailure(final Class<T> class_, final ThrowingConsumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        try {
            if (class_.isAssignableFrom(fault.getClass())) {
                consumer.accept(class_.cast(fault));
            }
        }
        catch (final Throwable throwable) {
            throwable.addSuppressed(fault);
            return Future.failure(throwable);
        }
        return this;
    }

    @Override
    public Future<V> and(final ThrowingConsumer<Result<V>> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        try {
            consumer.accept(Result.failure(fault));
        }
        catch (final Throwable throwable) {
            throwable.addSuppressed(fault);
            return Future.failure(throwable);
        }
        return this;
    }

    @Override
    public <U> Future<U> map(final ThrowingFunction<? super V, U> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return Future.failure(fault);
    }

    @Override
    public <U extends Throwable> Future<V> mapCatch(
        final Class<U> class_,
        final ThrowingFunction<U, ? extends V> mapper)
    {
        Objects.requireNonNull(class_, "class_");
        Objects.requireNonNull(mapper, "mapper");
        Throwable fault0;
        if (class_.isAssignableFrom(fault.getClass())) {
            try {
                return Future.success(mapper.apply(class_.cast(fault)));
            }
            catch (final Throwable throwable) {
                fault0 = throwable;
            }
        }
        else {
            fault0 = fault;
        }
        return Future.failure(fault0);
    }

    @Override
    public <T extends Throwable> Future<V> mapFault(
        final Class<T> class_,
        final ThrowingFunction<Throwable, Throwable> mapper)
    {
        Objects.requireNonNull(mapper, "mapper");
        Throwable fault0;
        if (class_.isAssignableFrom(fault.getClass())) {
            try {
                fault0 = mapper.apply(fault);
            }
            catch (final Throwable throwable) {
                fault0 = throwable;
            }
        }
        else {
            fault0 = fault;
        }
        return Future.failure(fault0);
    }

    @Override
    public <U> Future<U> mapResult(final ThrowingFunction<Result<V>, Result<U>> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        try {
            return new CertainResult<>(mapper.apply(Result.failure(fault)));
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public <U> Future<U> mapThrow(final ThrowingFunction<? super V, Throwable> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return Future.failure(fault);
    }

    @Override
    public <U> Future<U> flatMap(final ThrowingFunction<? super V, ? extends Future<U>> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return Future.failure(fault);
    }

    @Override
    public <U extends Throwable> Future<V> flatMapCatch(
        final Class<U> class_,
        final ThrowingFunction<U, ? extends Future<V>> mapper)
    {
        Objects.requireNonNull(class_, "class_");
        Objects.requireNonNull(mapper, "Expected mapper");
        Throwable fault0;
        if (class_.isAssignableFrom(fault.getClass())) {
            try {
                return mapper.apply(class_.cast(fault));
            }
            catch (final Throwable throwable) {
                fault0 = throwable;
            }
        }
        else {
            fault0 = fault;
        }
        return Future.failure(fault0);
    }

    @Override
    public <T extends Throwable> Future<V> flatMapFault(
        final Class<T> class_,
        final ThrowingFunction<Throwable, ? extends Future<Throwable>> mapper)
    {
        Objects.requireNonNull(class_, "Expected class_");
        Objects.requireNonNull(mapper, "Expected mapper");
        return new Future<>() {
            private Future<?> cancelTarget = null;
            private boolean isCancelled = false;

            @Override
            public void await(final Consumer<Result<V>> consumer) {
                if (isCancelled) {
                    return;
                }
                Throwable fault0;
                if (class_.isAssignableFrom(fault.getClass())) {
                    try {
                        final var future1 = mapper.apply(fault);
                        future1.await(result -> consumer.accept(Result.failure(result.isSuccess()
                            ? result.value()
                            : result.fault())));
                        cancelTarget = future1;
                        return;
                    }
                    catch (final Throwable throwable) {
                        fault0 = throwable;
                    }
                }
                else {
                    fault0 = fault;
                }
                consumer.accept(Result.failure(fault0));
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
    public <U> Future<U> andFlatRewrap(final ThrowingFunction<Result<V>, ? extends Future<U>> mapper) {
        try {
            return mapper.apply(Result.failure(fault));
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public Future<V> flatMapThrow(final ThrowingFunction<V, ? extends Future<? extends Throwable>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        return this;
    }

    @Override
    public <U> Future<U> put(final U value) {
        Objects.requireNonNull(value, "Expected value");
        return Future.failure(fault);
    }

    @Override
    public <U> Future<U> injectFault(final Throwable throwable) {
        Objects.requireNonNull(throwable, "Expected throwable");
        throwable.addSuppressed(fault);
        return Future.failure(throwable);
    }

    @Override
    public Future<V> andDelayFor(final Duration duration) {
        Objects.requireNonNull(duration, "Expected duration");
        return new Future<>() {
            private Future<?> cancelTarget = null;

            @Override
            public void await(final Consumer<Result<V>> consumer) {
                cancelTarget = Schedulers.fixed()
                    .schedule(duration, () -> consumer.accept(Result.failure(fault)));
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
    public Future<V> andDelayUntil(final Instant baseline) {
        Objects.requireNonNull(baseline, "Expected baseline");
        return new Future<>() {
            private Future<?> cancelTarget = null;

            @Override
            public void await(final Consumer<Result<V>> consumer) {
                final var duration = Duration.between(baseline, Instant.now());
                final var result = Result.<V>failure(fault);
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
    public Future<?> andDetach(final Consumer<V> consumer) {
        Objects.requireNonNull(consumer, "Expected consumer");
        return Future.failure(fault);
    }

    @Override
    public <U> Future<U> forkJoin(final ThrowingFunction<V, U> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        return Future.failure(fault);
    }

    @Override
    public V await() {
        throwFault();
        return null;
    }

    @Override
    public V await(final Duration timeout) {
        throwFault();
        return null;
    }

    @SuppressWarnings("unchecked")
    private <E extends Throwable> void throwFault() throws E {
        throw (E) fault;
    }

    @Override
    public String toString() {
        return "Future{fault=" + fault + '}';
    }
}
