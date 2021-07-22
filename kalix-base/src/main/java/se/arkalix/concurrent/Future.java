package se.arkalix.concurrent;

import se.arkalix.util.Failure;
import se.arkalix.util.Result;
import se.arkalix.util.Success;
import se.arkalix.util._internal.Throwables;
import se.arkalix.util.concurrent.FuturePublisher;

import java.util.Objects;
import java.util.function.*;

public interface Future<T> {
    void onCompletion(Consumer<? super Result<T>> consumer);

    default void onFailure(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer);

        onCompletion(result -> result.ifFailure(consumer));
    }

    default <U> Future<U> map(final Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new Promise<U>();

        onCompletion(result0 -> {
            final Result<U> result1;

            fail:
            if (result0.isSuccess()) {
                final U value;
                try {
                    value = mapper.apply(((Success<T>) result0).value());
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
                    result1 = Failure.of(throwable);
                    break fail;
                }
                result1 = Success.of(value);
            }
            else {
                result1 = (Failure<U>) result0;
            }

            promise.complete(result1);
        });

        return promise.future();
    }

    default <U> Future<U> flatMap(final Function<? super T, ? extends Future<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new Promise<U>();

        onCompletion(result -> {
            Throwable exception;

            fail:
            if (result.isSuccess()) {
                final Future<? extends U> future0;
                try {
                    future0 = mapper.apply(((Success<T>) result).value());
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
                    exception = throwable;
                    break fail;
                }
                if (future0 == null) {
                    exception = new NullPointerException();
                    break fail;
                }

                @SuppressWarnings("unchecked") final var future1 = (Future<U>) future0;

                promise.completeWith(future1);
                return;
            }
            else {
                exception = ((Failure<T>) result).exception();
            }
            promise.fail(exception);
        });

        return promise.future();
    }

    default Future<T> filter(final Predicate<? super T> predicate) {
        throw new UnsupportedOperationException(); // TODO: Implement.
    }

    default Result<T> filter(final Predicate<? super T> predicate, final Supplier<Throwable> failureSupplier) {
        throw new UnsupportedOperationException(); // TODO: Implement.
    }

    default Future<T> recover(final Function<Throwable, ? extends T> mapper) {
        throw new UnsupportedOperationException(); // TODO: Implement.
    }

    default Future<T> recoverWith(Function<Throwable, ? extends Future<? extends T>> mapper) {
        throw new UnsupportedOperationException(); // TODO: Implement.
    }

    default <U> Future<U> rewrap(final Function<? super Result<? super T>, ? extends Result<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new Promise<U>();

        onCompletion(result0 -> {
            Result<? extends U> result1;
            try {
                result1 = mapper.apply(result0);
            }
            catch (final Throwable throwable) {
                Throwables.throwSilentlyIfFatal(throwable);
                if (result0.isFailure()) {
                    throwable.addSuppressed(((Failure<T>) result0).exception());
                }
                result1 = Failure.of(throwable);
            }

            @SuppressWarnings("unchecked") final var result2 = (Result<U>) result1;

            promise.complete(result2);
        });

        return promise.future();
    }

    default <U> Future<U> rewrapWith(final Function<? super Result<? super T>, ? extends Future<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new Promise<U>();

        onCompletion(result -> {
            Throwable exception;

            fail:
            {
                final Future<? extends U> future0;
                try {
                    future0 = mapper.apply(result);
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
                    exception = throwable;
                    break fail;
                }
                if (future0 == null) {
                    exception = new NullPointerException();
                    break fail;
                }

                @SuppressWarnings("unchecked") final var future1 = (Future<U>) future0;

                promise.completeWith(future1);
                return;
            }
            promise.fail(exception);
        });

        return promise.future();
    }

    default <U, R> Future<R> zip(final Future<U> other, final BiFunction<? super T, ? super U, ? extends R> combinator) {
        throw new UnsupportedOperationException(); // TODO: Implement.
    }

    default <U, R> Future<R> zipWith(final Future<U> other, final BiFunction<? super T, ? super U, ? extends Future<? extends R>> combinator) {
        throw new UnsupportedOperationException(); // TODO: Implement.
    }

    default Future<T> and(final Runnable action) {
        Objects.requireNonNull(action);

        final var promise = new Promise<T>();

        onCompletion(result -> {
            Throwable exception;

            fail:
            {
                try {
                    action.run();
                }
                catch (final Throwable throwable) {
                    if (result.isFailure()) {
                        throwable.addSuppressed(((Failure<T>) result).exception());
                    }
                    exception = throwable;
                    break fail;
                }

                promise.complete(result);
                return;
            }

            promise.fail(exception);
        });

        return promise.future();
    }

    default Future<T> and(final Consumer<? super Result<? super T>> consumer) {
        Objects.requireNonNull(consumer);

        final var promise = new Promise<T>();

        onCompletion(result -> {
            Throwable exception;

            fail:
            {
                try {
                    consumer.accept(result);
                }
                catch (final Throwable throwable) {
                    if (result.isFailure()) {
                        throwable.addSuppressed(((Failure<T>) result).exception());
                    }
                    exception = throwable;
                    break fail;
                }

                promise.complete(result);
                return;
            }

            promise.fail(exception);
        });

        return promise.future();
    }

    default <U> Future<T> and(final Supplier<? extends U> supplier) {
        throw new UnsupportedOperationException(); // TODO: Implement.
    }

    default <U> Future<T> and(final Function<? super Result<? super T>, ? extends U> mapper) {
        throw new UnsupportedOperationException(); // TODO: Implement.
    }

    default <U> Future<T> andWith(final Function<? super Result<? super T>, ? extends Future<? extends U>> mapper) {
        throw new UnsupportedOperationException(); // TODO: Implement.
    }

    default FuturePublisher<T> toPublisher() {
        throw new UnsupportedOperationException(); // TODO: Implement.
    }

    static <T> Future<T> of(final Result<T> result) {
        if (result.isSuccess()) {
            return success(((Success<T>) result).value());
        }
        else {
            return failure(((Failure<T>) result).exception());
        }
    }

    static <T> Future<T> success(final T value) {
        throw new UnsupportedOperationException(); // TODO: Implement.
    }

    static <T> Future<T> failure(final Throwable exception) {
        throw new UnsupportedOperationException(); // TODO: Implement.
    }

    static Future<?> done() {
        throw new UnsupportedOperationException(); // TODO: Implement.
    }
}
