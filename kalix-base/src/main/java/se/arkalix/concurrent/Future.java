package se.arkalix.concurrent;

import se.arkalix.concurrent._internal.ChainedPromise;
import se.arkalix.util.Result;
import se.arkalix.util._internal.Throwables;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Future<T> {
    void onCompletion(Consumer<? super Result<T>> consumer);

    default void onFailure(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer);

        onCompletion(result -> result.ifFailure(consumer));
    }

    default <U> Future<U> map(final Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new ChainedPromise<U>(this);

        onCompletion(result -> result.ifSuccessOrElse(
            value0 -> {
                final U value1;
                try {
                    value1 = mapper.apply(value0);
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
                    promise.fail(throwable);
                    return;
                }
                promise.fulfill(value1);
            },
            promise::fail
        ));

        return promise.future();
    }

    default <U> Future<U> flatMap(final Function<? super T, ? extends Future<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new ChainedPromise<U>(this);

        onCompletion(result0 -> result0.ifSuccessOrElse(
            value0 -> Objects.requireNonNull(mapper.apply(value0))
                .onCompletion(result1 -> result1.ifSuccessOrElse(
                    promise::fulfill,
                    promise::fail)),
            promise::fail
        ));

        return promise.future();
    }

    default <U> Future<U> rewrap(final Function<? super Result<? super T>, ? extends Result<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new ChainedPromise<U>(this);

        onCompletion(result0 -> Objects.requireNonNull(mapper.apply(result0))
            .ifSuccessOrElse(
                promise::fulfill,
                promise::fail));

        return promise.future();
    }
}
