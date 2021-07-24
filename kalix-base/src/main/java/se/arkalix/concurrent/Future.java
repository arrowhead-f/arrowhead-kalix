package se.arkalix.concurrent;

import se.arkalix.util.Failure;
import se.arkalix.util.Result;
import se.arkalix.util.Success;
import se.arkalix.util._internal.Throwables;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;

@FunctionalInterface
public interface Future<T> {
    void onCompletion(Consumer<? super Result<T>> consumer);

    default void onFailure(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer);

        onCompletion(result -> result.ifFailure(consumer));
    }

    default <U> Future<U> map(final Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new PromiseForSameThread<U>();

        onCompletion(result0 -> {
            final Result<U> result1;

            fail:
            if (result0 instanceof Success<T> success) {
                final U value;

                try {
                    value = mapper.apply(success.value());
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

        final var promise = new PromiseForSameThread<U>();

        onCompletion(result -> {
            final Throwable exception;

            fail:
            if (result instanceof Success<T> success) {
                final Future<? extends U> future0;

                try {
                    future0 = mapper.apply(success.value());
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

                promise.complete(future1);

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
        Objects.requireNonNull(predicate);

        final var promise = new PromiseForSameThread<T>();

        onCompletion(result0 -> {
            final Result<T> result1;

            fail:
            {
                if (result0 instanceof Success<T> success) {
                    final boolean isMatch;

                    try {
                        isMatch = predicate.test(success.value());
                    }
                    catch (final Throwable throwable) {
                        Throwables.throwSilentlyIfFatal(throwable);
                        result1 = Failure.of(throwable);
                        break fail;
                    }

                    if (!isMatch) {
                        result1 = Failure.of(new NoSuchElementException());
                        break fail;
                    }
                }
                result1 = result0;
            }

            promise.complete(result1);
        });

        return promise.future();
    }

    default Future<T> filter(final Predicate<? super T> predicate, final Supplier<Throwable> failureSupplier) {
        Objects.requireNonNull(predicate);

        final var promise = new PromiseForSameThread<T>();

        onCompletion(result0 -> {
            final Result<T> result1;

            fail:
            {
                pass:
                if (result0 instanceof Success<T> success) {
                    final boolean isMatch;

                    try {
                        isMatch = predicate.test(success.value());
                    }
                    catch (final Throwable throwable) {
                        Throwables.throwSilentlyIfFatal(throwable);
                        result1 = Failure.of(throwable);
                        break fail;
                    }

                    if (isMatch) {
                        break pass;
                    }

                    Throwable exception;

                    try {
                        exception = failureSupplier.get();
                    }
                    catch (final Throwable throwable) {
                        Throwables.throwSilentlyIfFatal(throwable);
                        exception = throwable;
                    }

                    result1 = Failure.of(exception);

                    break fail;
                }
                result1 = result0;
            }

            promise.complete(result1);
        });

        return promise.future();
    }

    default Future<T> reject(final Supplier<Throwable> supplier) {
        Objects.requireNonNull(supplier);

        final var promise = new PromiseForSameThread<T>();

        onCompletion(result0 -> {
            final Result<T> result1;

            if (result0.isSuccess()) {
                Throwable exception;

                try {
                    exception = supplier.get();
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
                    exception = throwable;
                }

                result1 = Failure.of(exception);
            }
            else {
                result1 = result0;
            }

            promise.complete(result1);
        });

        return promise.future();
    }

    default Future<T> reject(final Function<? super T, Throwable> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new PromiseForSameThread<T>();

        onCompletion(result0 -> {
            final Result<T> result1;

            if (result0 instanceof Success<T> success) {
                Throwable exception;

                try {
                    exception = mapper.apply(success.value());
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
                    exception = throwable;
                }

                result1 = Failure.of(exception);
            }
            else {
                result1 = result0;
            }

            promise.complete(result1);
        });

        return promise.future();
    }

    default Future<T> recover(final Function<Throwable, ? extends T> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new PromiseForSameThread<T>();

        onCompletion(result0 -> {
            Result<T> result1;

            if (result0 instanceof Failure<T> failure) {
                final var exception = failure.exception();

                try {
                    result1 = Success.of(mapper.apply(exception));
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
                    throwable.addSuppressed(exception);
                    result1 = Failure.of(throwable);
                }
            }
            else {
                result1 = result0;
            }

            promise.complete(result1);
        });

        return promise.future();
    }

    default Future<T> flatRecover(Function<Throwable, ? extends Future<? extends T>> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new PromiseForSameThread<T>();

        onCompletion(result0 -> {
            final Result<T> result1;

            fail:
            if (result0 instanceof Failure<T> failure) {
                final var exception = failure.exception();

                final Future<? extends T> future0;

                try {
                    future0 = mapper.apply(exception);
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
                    throwable.addSuppressed(exception);
                    result1 = Failure.of(throwable);
                    break fail;
                }

                @SuppressWarnings("unchecked") final var future1 = (Future<T>) future0;

                promise.complete(future1);

                return;
            }
            else {
                result1 = result0;
            }

            promise.complete(result1);
        });

        return promise.future();
    }

    default <U> Future<U> rewrap(final Function<? super Result<? super T>, ? extends Result<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new PromiseForSameThread<U>();

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

    default <U> Future<U> flatRewrap(final Function<? super Result<? super T>, ? extends Future<? extends U>> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new PromiseForSameThread<U>();

        onCompletion(result -> {
            final Throwable exception;

            fail:
            {
                final Future<? extends U> future0;

                try {
                    future0 = mapper.apply(result);
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
                    if (result instanceof Failure<T> failure) {
                        throwable.addSuppressed(failure.exception());
                    }
                    exception = throwable;
                    break fail;
                }

                if (future0 == null) {
                    exception = new NullPointerException();
                    break fail;
                }

                @SuppressWarnings("unchecked") final var future1 = (Future<U>) future0;

                promise.complete(future1);

                return;
            }
            promise.fail(exception);
        });

        return promise.future();
    }

    default <U, R> Future<R> zip(final Future<U> other, final BiFunction<? super T, ? super U, ? extends R> combinator) {
        Objects.requireNonNull(other);
        Objects.requireNonNull(combinator);

        final var promise = new PromiseForSameThread<R>();

        onCompletion(thisResult -> other.onCompletion(otherResult -> {
            final Result<R> result;

            fail:
            {
                if (thisResult instanceof Failure<T> thisFailure) {
                    if (otherResult instanceof Failure<U> otherFailure) {
                        final var exception = thisFailure.exception();
                        exception.addSuppressed(otherFailure.exception());
                        result = Failure.of(exception);
                    }
                    else {
                        result = thisFailure.retype();
                    }
                    break fail;
                }

                if (otherResult instanceof Failure<U> otherFailure) {
                    result = otherFailure.retype();
                    break fail;
                }

                final R value;
                final var thisSuccess = (Success<T>) thisResult;
                final var otherSuccess = (Success<U>) otherResult;

                try {
                    value = combinator.apply(thisSuccess.value(), otherSuccess.value());
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
                    result = Failure.of(throwable);
                    break fail;
                }

                result = Success.of(value);
            }

            promise.complete(result);
        }));

        return promise.future();
    }

    default <U, R> Future<R> flatZip(final Future<U> other, final BiFunction<? super T, ? super U, ? extends Future<? extends R>> combinator) {
        Objects.requireNonNull(other);
        Objects.requireNonNull(combinator);

        final var promise = new PromiseForSameThread<R>();

        onCompletion(thisResult -> other.onCompletion(otherResult -> {
            final Result<R> result;

            fail:
            {
                if (thisResult instanceof Failure<T> thisFailure) {
                    if (otherResult instanceof Failure<U> otherFailure) {
                        final var exception = thisFailure.exception();
                        exception.addSuppressed(otherFailure.exception());
                        result = Failure.of(exception);
                    }
                    else {
                        result = thisFailure.retype();
                    }
                    break fail;
                }

                if (otherResult instanceof Failure<U> otherFailure) {
                    result = otherFailure.retype();
                    break fail;
                }

                final Future<? extends R> future0;
                final var thisSuccess = (Success<T>) thisResult;
                final var otherSuccess = (Success<U>) otherResult;

                try {
                    future0 = combinator.apply(thisSuccess.value(), otherSuccess.value());
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
        }));

        return promise.future();
    }

    default Future<T> then(final Runnable action) {
        Objects.requireNonNull(action);

        final var promise = new PromiseForSameThread<T>();

        onCompletion(result -> {
            Throwable exception;

            fail:
            {
                try {
                    action.run();
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
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

    default Future<T> then(final Consumer<? super Result<? super T>> consumer) {
        Objects.requireNonNull(consumer);

        final var promise = new PromiseForSameThread<T>();

        onCompletion(result -> {
            Throwable exception;

            fail:
            {
                try {
                    consumer.accept(result);
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
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

    default <U> Future<T> then(final Supplier<? extends U> supplier) {
        Objects.requireNonNull(supplier);

        final var promise = new PromiseForSameThread<T>();

        onCompletion(result -> {
            Throwable exception;

            fail:
            {
                try {
                    supplier.get();
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
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

    default <U> Future<T> then(final Function<? super Result<? super T>, ? extends U> mapper) {
        Objects.requireNonNull(mapper);

        final var promise = new PromiseForSameThread<T>();

        onCompletion(result -> {
            Throwable exception;

            fail:
            {
                try {
                    mapper.apply(result);
                }
                catch (final Throwable throwable) {
                    Throwables.throwSilentlyIfFatal(throwable);
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

    default FuturePublisher<T> toPublisher() {
        return new FuturePublisher<>(this);
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
        return new FutureSuccess<>(value);
    }

    static <T> Future<T> failure(final Throwable exception) {
        return new FutureFailure<>(exception);
    }

    static Future<Void> done() {
        return FutureSuccess.empty();
    }
}
