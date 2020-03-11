package eu.arrowhead.kalix.util.concurrent;

import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.function.ThrowingFunction;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class FutureResult<V> implements Future<V> {
    private final Result<V> result;

    private boolean isCompleted = false;

    public FutureResult(final Result<V> result) {
        this.result = result;
    }

    @Override
    public void onResult(final Consumer<Result<V>> consumer) {
        Objects.requireNonNull(consumer, "Expected consumer");
        if (isCompleted) {
            return;
        }
        isCompleted = true;
        consumer.accept(result);
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        // Does nothing.
    }

    @Override
    public void onFailure(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer, "Expected consumer");
        if (isCompleted) {
            return;
        }
        isCompleted = true;
        if (!result.isSuccess()) {
            consumer.accept(result.fault());
        }
    }

    @Override
    public <U> Future<U> map(final ThrowingFunction<? super V, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        Throwable fault;
        if (result.isSuccess()) {
            try {
                return new FutureSuccess<>(mapper.apply(result.value()));
            }
            catch (final Throwable throwable) {
                fault = throwable;
            }
        }
        else {
            fault = result.fault();
        }
        return new FutureFailure<>(fault);
    }

    @Override
    public Future<V> mapCatch(final ThrowingFunction<Throwable, ? extends V> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        if (result.isSuccess()) {
            return this;
        }
        try {
            return new FutureSuccess<>(mapper.apply(result.fault()));
        }
        catch (final Throwable throwable) {
            return new FutureFailure<>(throwable);
        }
    }

    @Override
    public Future<V> mapError(final ThrowingFunction<Throwable, Throwable> mapper) {
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
        return new FutureFailure<>(fault);
    }

    @Override
    public <U> Future<U> mapResult(final ThrowingFunction<Result<? super V>, Result<U>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        try {
            return new FutureResult<>(mapper.apply(result));
        }
        catch (final Throwable throwable) {
            return new FutureFailure<>(throwable);
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
        return new FutureFailure<>(fault);
    }

    @Override
    public Future<V> flatMapCatch(final ThrowingFunction<Throwable, ? extends Future<V>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        if (result.isSuccess()) {
            return this;
        }
        try {
            return mapper.apply(result.fault());
        }
        catch (final Throwable throwable) {
            return new FutureFailure<>(throwable);
        }
    }

    @Override
    public Future<V> flatMapError(final ThrowingFunction<Throwable, ? extends Future<? extends Throwable>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        if (result.isSuccess()) {
            return this;
        }
        final var cancelTarget = new AtomicReference<Future<?>>();
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                try {
                    final var future1 = mapper.apply(result.fault());
                    future1.onResult(result -> consumer.accept(Result.failure(result.isSuccess()
                        ? result.value()
                        : result.fault())));
                    cancelTarget.set(future1);
                }
                catch (final Throwable throwable) {
                    consumer.accept(Result.failure(throwable));
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

    @Override
    public <U> Future<U> flatMapResult(final ThrowingFunction<Result<V>, ? extends Future<U>> mapper) {
        try {
            return mapper.apply(result);
        }
        catch (final Throwable throwable) {
            return new FutureFailure<>(throwable);
        }
    }
}
