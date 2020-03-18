package eu.arrowhead.kalix.util.concurrent;

import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.function.ThrowingFunction;

import java.util.Objects;
import java.util.function.Consumer;

public class FutureResult<V> implements Future<V> {
    private final Result<V> result;

    public FutureResult(final Result<V> result) {
        this.result = result;
    }

    @Override
    public void onResult(final Consumer<Result<V>> consumer) {
        Objects.requireNonNull(consumer, "Expected consumer");
        consumer.accept(result);
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        // Does nothing.
    }

    @Override
    public void onFailure(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer, "Expected consumer");
        if (result.isFailure()) {
            consumer.accept(result.fault());
        }
    }

    @Override
    public <U> Future<U> map(final ThrowingFunction<? super V, U> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
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
        Objects.requireNonNull(class_, "Expected class_");
        Objects.requireNonNull(mapper, "Expected mapper");
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
    public <U> Future<U> mapThrow(final ThrowingFunction<? super V, Throwable> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
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
    public Future<V> mapFault(final ThrowingFunction<Throwable, Throwable> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        if (result.isSuccess()) {
            return this;
        }
        Throwable fault;
        try {
            fault = mapper.apply(result.fault());
        }
        catch (final Throwable throwable) {
            fault = throwable;
        }
        return Future.failure(fault);
    }

    @Override
    public <U> Future<U> mapResult(final ThrowingFunction<Result<V>, Result<U>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        try {
            return Future.of(mapper.apply(result));
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public <U> Future<U> flatMap(final ThrowingFunction<? super V, ? extends Future<U>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
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
        Objects.requireNonNull(class_, "Expected class_");
        Objects.requireNonNull(mapper, "Expected mapper");
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
    public Future<V> flatMapFault(final ThrowingFunction<Throwable, ? extends Future<Throwable>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
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
                try {
                    final var future1 = mapper.apply(result.fault());
                    future1.onResult(result -> consumer.accept(Result.failure(result.isSuccess()
                        ? result.value()
                        : result.fault())));
                    cancelTarget = future1;
                }
                catch (final Throwable throwable) {
                    consumer.accept(Result.failure(throwable));
                }
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                isCancelled = true;
                if (cancelTarget != null) {
                    cancelTarget.cancel(mayInterruptIfRunning);
                    cancelTarget = null;
                }
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
    public <U> Future<U> pass(final U value) {
        Objects.requireNonNull(value, "Expected value");
        return result.isSuccess()
            ? Future.success(value)
            : Future.failure(result.fault());
    }

    @Override
    public <U> Future<U> fail(final Throwable throwable) {
        Objects.requireNonNull(throwable, "Expected throwable");
        if (result.isFailure()) {
            throwable.addSuppressed(result.fault());
        }
        return Future.failure(throwable);
    }
}
