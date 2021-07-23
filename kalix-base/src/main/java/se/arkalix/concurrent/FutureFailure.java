package se.arkalix.concurrent;

import se.arkalix.util.Failure;
import se.arkalix.util.Result;
import se.arkalix.util._internal.Throwables;

import java.util.Objects;
import java.util.function.*;

public record FutureFailure<T>(Throwable exception) implements Future<T> {
    public FutureFailure {
        Throwables.throwSilentlyIfFatalOrNull(exception);
    }

    @Override
    public void onCompletion(final Consumer<? super Result<T>> consumer) {
        Objects.requireNonNull(consumer)
            .accept(Failure.of(exception));
    }

    @Override
    public void onFailure(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer)
            .accept(exception);
    }

    @Override
    public <U> Future<U> map(final Function<? super T, ? extends U> mapper) {
        return Future.failure(exception);
    }

    @Override
    public <U> Future<U> flatMap(final Function<? super T, ? extends Future<? extends U>> mapper) {
        return Future.failure(exception);
    }

    @Override
    public Future<T> filter(final Predicate<? super T> predicate) {
        return Future.failure(exception);
    }

    @Override
    public Future<T> filter(final Predicate<? super T> predicate, final Supplier<Throwable> failureSupplier) {
        return Future.failure(exception);
    }

    @Override
    public Future<T> reject(final Supplier<Throwable> supplier) {
        return Future.failure(exception);
    }

    @Override
    public Future<T> reject(final Function<? super T, Throwable> mapper) {
        return Future.failure(exception);
    }

    @Override
    public Future<T> recover(final Function<Throwable, ? extends T> mapper) {
        Objects.requireNonNull(mapper);

        try {
            return Future.success(mapper.apply(exception));
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            throwable.addSuppressed(exception);
            return Future.failure(throwable);
        }
    }

    @Override
    public Future<T> flatRecover(final Function<Throwable, ? extends Future<? extends T>> mapper) {
        Objects.requireNonNull(mapper);

        final Future<? extends T> future0;

        try {
            future0 = mapper.apply(exception);
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            throwable.addSuppressed(exception);
            return Future.failure(throwable);
        }

        @SuppressWarnings("unchecked") final var future1 = (Future<T>) future0;

        return future1;
    }

    @Override
    public <U> Future<U> rewrap(final Function<? super Result<? super T>, ? extends Result<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        Result<? extends U> result0;

        try {
            result0 = mapper.apply(Failure.of(exception));
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            throwable.addSuppressed(exception);
            result0 = Failure.of(throwable);
        }

        @SuppressWarnings("unchecked") final var result1 = (Result<U>) result0;

        return Future.of(result1);
    }

    @Override
    public <U> Future<U> flatRewrap(final Function<? super Result<? super T>, ? extends Future<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        final Throwable exception;

        fail:
        {
            final Future<? extends U> future0;

            try {
                future0 = mapper.apply(Failure.of(this.exception));
            }
            catch (final Throwable throwable) {
                Throwables.throwSilentlyIfFatal(throwable);
                throwable.addSuppressed(this.exception);
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
            if (otherResult instanceof Failure<U> otherFailure) {
                exception.addSuppressed(otherFailure.exception());
            }

            promise.fail(exception);
        });

        return promise.future();
    }

    @Override
    public <U, R> Future<R> flatZip(final Future<U> other, final BiFunction<? super T, ? super U, ? extends Future<? extends R>> combinator) {
        Objects.requireNonNull(other);
        Objects.requireNonNull(combinator);

        final var promise = new UnsynchronizedPromise<R>();

        other.onCompletion(otherResult -> {
            if (otherResult instanceof Failure<U> otherFailure) {
                exception.addSuppressed(otherFailure.exception());
            }

            promise.fail(exception);
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
            consumer.accept(Failure.of(exception));
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
            mapper.apply(Failure.of(exception));
        }
        catch (final Throwable throwable) {
            Throwables.throwSilentlyIfFatal(throwable);
            return Future.failure(throwable);
        }

        return this;
    }
}
