package se.arkalix.concurrent;

import se.arkalix.concurrent._internal.ChainedPromise;
import se.arkalix.util.Result;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Future<T> {
    void onResult(Consumer<? super Result<T>> consumer);

    default void onFault(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer);

        onResult(result -> result.ifFailure(consumer));
    }

    boolean cancel();

    default <U> Future<U> map(final Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new ChainedPromise<U>(this);

        onResult(result -> result.ifSuccessOrElse(
            value -> promise.fulfill(mapper.apply(value)),
            promise::forfeit
        ));

        return promise.future();
    }

    default <U> Future<U> flatMap(final Function<? super T, ? extends Future<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new ChainedPromise<U>(this);

        onResult(result0 -> result0.ifSuccessOrElse(
            value0 -> Objects.requireNonNull(mapper.apply(value0))
                .onResult(result1 -> result1.ifSuccessOrElse(
                    promise::fulfill,
                    promise::forfeit)),
            promise::forfeit
        ));

        return promise.future();
    }

    default <U> Future<U> rewrap(final Function<? super Result<? super T>, ? extends Result<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new ChainedPromise<U>(this);

        onResult(result0 -> Objects.requireNonNull(mapper.apply(result0))
            .ifSuccessOrElse(
                promise::fulfill,
                promise::forfeit));

        return promise.future();
    }
}
