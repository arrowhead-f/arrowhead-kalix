package se.arkalix.concurrent;

import se.arkalix.util.Failure;
import se.arkalix.util.Result;
import se.arkalix.util.Success;
import se.arkalix.util._internal.Throwables;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;

public record FutureSuccess<T>(T value) implements Future<T> {
    private static final FutureSuccess<Void> EMPTY = new FutureSuccess<>(null);

    public static FutureSuccess<Void> empty() {
        return EMPTY;
    }

    @Override
    public void onCompletion(final Consumer<? super Result<T>> consumer) {
        Objects.requireNonNull(consumer)
            .accept(Success.of(value));
    }

    @Override
    public void onFailure(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer);
    }

    @Override
    public <U> Future<U> map(final Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);

        final Result<U> result1;
        fail:
        {
            final U value;

            try {
                value = mapper.apply(this.value);
            }
            catch (final Throwable throwable) {
                Throwables.throwSilentlyIfFatal(throwable);
                result1 = Failure.of(throwable);
                break fail;
            }

            result1 = Success.of(value);
        }

        return Future.of(result1);
    }

    @Override
    public <U> Future<U> flatMap(final Function<? super T, ? extends Future<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        final Throwable exception;

        fail:
        {
            final Future<? extends U> future0;

            try {
                future0 = mapper.apply(value);
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

            return future1;
        }

        return Future.failure(exception);
    }

    @Override
    public Future<T> filter(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);

        final Throwable exception;

        fail:
        {
            final boolean isMatch;

            try {
                isMatch = predicate.test(value);
            }
            catch (final Throwable throwable) {
                Throwables.throwSilentlyIfFatal(throwable);
                exception = throwable;
                break fail;
            }

            if (!isMatch) {
                exception = new NoSuchElementException();
                break fail;
            }

            return this;
        }

        return Future.failure(exception);
    }

    @Override
    public Future<T> filter(final Predicate<? super T> predicate, final Supplier<Throwable> failureSupplier) {
        Objects.requireNonNull(predicate);

        Throwable exception;

        fail:
        {
            final boolean isMatch;

            try {
                isMatch = predicate.test(value);
            }
            catch (final Throwable throwable) {
                Throwables.throwSilentlyIfFatal(throwable);
                exception = throwable;
                break fail;
            }

            if (isMatch) {
                return this;
            }

            try {
                exception = failureSupplier.get();
            }
            catch (final Throwable throwable) {
                Throwables.throwSilentlyIfFatal(throwable);
                exception = throwable;
            }
        }

        return Future.failure(exception);
    }

    @Override
    public Future<T> reject(final Supplier<Throwable> supplier) {
        Objects.requireNonNull(supplier);

        Throwable exception;

        try {
            exception = supplier.get();
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            exception = throwable;
        }

        return Future.failure(exception);
    }

    @Override
    public Future<T> reject(final Function<? super T, Throwable> mapper) {
        Objects.requireNonNull(mapper);

        Throwable exception;

        try {
            exception = mapper.apply(value);
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            exception = throwable;
        }

        return Future.failure(exception);
    }

    @Override
    public Future<T> recover(final Function<Throwable, ? extends T> mapper) {
        Objects.requireNonNull(mapper);

        return this;
    }

    @Override
    public Future<T> flatRecover(final Function<Throwable, ? extends Future<? extends T>> mapper) {
        Objects.requireNonNull(mapper);

        return this;
    }

    @Override
    public <U> Future<U> rewrap(final Function<? super Result<? super T>, ? extends Result<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        final Result<U> result0;

        fail:
        {
            Result<? extends U> result1;

            try {
                result1 = mapper.apply(Success.of(value));
            }
            catch (final Throwable throwable) {
                Throwables.throwSilentlyIfFatal(throwable);
                result0 = Failure.of(throwable);
                break fail;
            }

            if (result1 == null) {
                result0 = Failure.of(new NullPointerException());
                break fail;
            }

            @SuppressWarnings("unchecked") final var result2 = (Result<U>) result1;

            result0 = result2;
        }

        return Future.of(result0);
    }

    @Override
    public <U> Future<U> flatRewrap(final Function<? super Result<? super T>, ? extends Future<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        Throwable exception;

        fail:
        {
            final Future<? extends U> future0;

            try {
                future0 = mapper.apply(Success.of(value));
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

            return future1;
        }

        return Future.failure(exception);
    }

    @Override
    public <U, R> Future<R> zip(final Future<U> other, final BiFunction<? super T, ? super U, ? extends R> combinator) {
        Objects.requireNonNull(other);
        Objects.requireNonNull(combinator);

        final var promise = new UnsynchronizedPromise<R>();

        other.onCompletion(otherResult -> {
            final Result<R> result;

            fail:
            {
                if (otherResult instanceof Failure<U> otherFailure) {
                    result = otherFailure.retype();
                    break fail;
                }

                final R value;
                final var otherSuccess = (Success<U>) otherResult;

                try {
                    value = combinator.apply(this.value, otherSuccess.value());
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
                    result = Failure.of(throwable);
                    break fail;
                }

                result = Success.of(value);
            }

            promise.complete(result);
        });

        return promise.future();
    }

    @Override
    public <U, R> Future<R> flatZip(final Future<U> other, final BiFunction<? super T, ? super U, ? extends Future<? extends R>> combinator) {
        Objects.requireNonNull(other);
        Objects.requireNonNull(combinator);

        final var promise = new UnsynchronizedPromise<R>();

        other.onCompletion(otherResult -> {
            final Result<R> result;

            fail:
            {
                if (otherResult instanceof Failure<U> otherFailure) {
                    result = otherFailure.retype();
                    break fail;
                }

                final Future<? extends R> future0;
                final var otherSuccess = (Success<U>) otherResult;

                try {
                    future0 = combinator.apply(this.value, otherSuccess.value());
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
                    result = Failure.of(throwable);
                    break fail;
                }

                if (future0 == null) {
                    result = Failure.of(new NullPointerException());
                    break fail;
                }

                @SuppressWarnings("unchecked") final var future1 = (Future<R>) future0;

                promise.complete(future1);

                return;
            }

            promise.complete(result);
        });

        return promise.future();
    }

    @Override
    public Future<T> and(final Runnable action) {
        Objects.requireNonNull(action);

        try {
            action.run();
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            return Future.failure(throwable);
        }

        return this;
    }

    @Override
    public Future<T> and(final Consumer<? super Result<? super T>> consumer) {
        Objects.requireNonNull(consumer);

        try {
            consumer.accept(Success.of(value));
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            return Future.failure(throwable);
        }

        return this;
    }

    @Override
    public <U> Future<T> and(final Supplier<? extends U> supplier) {
        Objects.requireNonNull(supplier);

        try {
            supplier.get();
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            return Future.failure(throwable);
        }

        return this;
    }

    @Override
    public <U> Future<T> and(final Function<? super Result<? super T>, ? extends U> mapper) {
        Objects.requireNonNull(mapper);

        try {
            mapper.apply(Success.of(value));
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            return Future.failure(throwable);
        }

        return this;
    }
}
