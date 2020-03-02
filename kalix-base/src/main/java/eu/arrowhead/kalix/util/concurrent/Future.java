package eu.arrowhead.kalix.util.concurrent;

import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.function.ThrowingFunction;

import java.util.Objects;
import java.util.concurrent.atomic.*;
import java.util.function.Consumer;

/**
 * Represents an operation that will complete at some point in the future.
 * <p>
 * To make it convenient to act on the completion of this {@code Future}, the
 * receiver of it is expected to provide a {@code Consumer} to the
 * {@link #onResult(Consumer)} method, which should be invoked whenever the
 * {@link Result} of the operation becomes available. As an alternative, the
 * {@code Future} receiver may also decide to {@link #cancel(boolean)} it,
 * which should mean that any {@code Consumer}, if set, never will be invoked.
 * <p>
 * It is the responsibility of the receiver of a {@code Future} to make sure
 * that either {@link #onResult(Consumer)} and/or {@link #cancel(boolean)} is
 * called, or any method that uses one of them internally, as it may be the
 * case that the operation represented by the future will not start running
 * until either of these methods are invoked. Failing to call any of these
 * methods may lead to memory never being reclaimed.
 *
 * @param <V> Type of value that can be retrieved if the operation succeeds.
 */
public interface Future<V> {
    /**
     * Sets function to receive result of this {@code Future}, when it becomes
     * available.
     * <p>
     * If this method has been called previously on the same object, any
     * previously set consumer should be replaced by the one given.
     * <p>
     * The given consumer function should never throw exceptions that need to
     * be handled explicitly. Any thrown exception will end up with the caller
     * of the function, which is unlikely to be able to handle it in any other
     * way than by logging it.
     *
     * @param consumer Function invoked when this {@code Future} completes.
     * @throws NullPointerException If {@code consumer} is {@code null}.
     */
    void onResult(final Consumer<Result<V>> consumer);

    /**
     * Signals that the result of this {@code Future} no longer is of interest.
     * <p>
     * If this {@code Future} has already been cancelled or completed, calling
     * this method should do nothing. Calling this method on a {@code Future}
     * that has not yet completed should prevent {@link #onResult(Consumer)}
     * from ever being called.
     *
     * @param mayInterruptIfRunning Whether or not the thread executing the
     *                              task associated with this {@code Future},
     *                              should be interrupted. If not, in-progress
     *                              tasks are allowed to complete.
     */
    void cancel(final boolean mayInterruptIfRunning);

    /**
     * Signals that the result of this {@code Future} no longer is of interest
     * and that evaluation of the {@code Future} should be gracefully
     * terminated.
     * <p>
     * If this {@code Future} has already been cancelled or completed, calling
     * this method should do nothing. Calling this method on a {@code Future}
     * that has not yet completed should prevent {@link #onResult(Consumer)}
     * from ever being called.
     */
    default void cancel() {
        cancel(false);
    }

    /**
     * Sets function to receive result of this {@code Future} only if its
     * operation fails.
     * <p>
     * Successful results are ignored.
     * <p>
     * While it might seem like a logical addition, there is no corresponding
     * {@code #onValue(Consumer)} method. The reason for this is that there is
     * no reasonable default strategy for handling ignored errors. Silently
     * discarding them is unlikely to be a suitable design choice, as it could
     * hide details important for discovering or tracking application issues,
     * and logging them would necessitate information about the context in
     * which the successful result was required, as well as integration against
     * a logging framework.
     *
     * @param consumer Function invoked if this {@code Future} completes with
     *                 an error.
     * @throws NullPointerException If {@code consumer} is {@code null}.
     */
    default void onError(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer);
        onResult(result -> {
            if (!result.isSuccess()) {
                consumer.accept(result.error());
            }
        });
    }

    /**
     * Returns new {@code Future} that is completed after the value of this
     * {@code Future} has become available and could be transformed into a
     * value of type {@code U} by {@code mapper}.
     * <p>
     * Any exception thrown by {@code mapper} should lead to the returned
     * future being failed with the same exception.
     *
     * @param <U>    The type of the value returned from the mapping function.
     * @param mapper The mapping function to apply to the value of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}, if it completes
     * successfully.
     * @throws NullPointerException If the mapping function is {@code null}.
     */
    default <U> Future<U> map(final ThrowingFunction<? super V, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        final var source = this;
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<U>> consumer) {
                source.onResult(r0 -> {
                    Result<U> r1;
                    success:
                    {
                        Throwable err;
                        if (r0.isSuccess()) {
                            try {
                                r1 = Result.success(mapper.apply(r0.value()));
                                break success;
                            }
                            catch (final Throwable error) {
                                err = error;
                            }
                        }
                        else {
                            err = r0.error();
                        }
                        r1 = Result.failure(err);
                    }
                    consumer.accept(r1);
                });
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                source.cancel(mayInterruptIfRunning);
            }
        };
    }

    /**
     * Returns new {@code Future} that is completed when the {@code Future}
     * returned by {@code mapper} completes, which, in turn, is not executed
     * until this {@code Future} completes.
     * <p>
     * The difference between this method and {@link #map(ThrowingFunction)} is that the
     * {@code mapper} provided here is expected to return a {@code Future}
     * rather than a plain value. The returned {@code Future} completes after
     * this {@code Future} and the {@code Future} returned by {@code mapper}
     * have completed in sequence.
     * <p>
     * Any exception thrown by {@code mapper} should lead to the returned
     * future being failed with the same exception.
     *
     * @param <U>    The type of the value returned from the mapping function.
     * @param mapper The mapping function to apply to the value of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}, and then waiting
     * for the {@code Future} returned by the mapper to complete.
     * @throws NullPointerException If the mapping function is {@code null}.
     */
    default <U> Future<U> flatMap(final ThrowingFunction<? super V, ? extends Future<U>> mapper) {
        Objects.requireNonNull(mapper);
        final var source = this;
        final var cancelTarget = new AtomicReference<Future<?>>(this);
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<U>> consumer) {
                source.onResult(r0 -> {
                    Throwable err;
                    if (r0.isSuccess()) {
                        try {
                            final var f1 = mapper.apply(r0.value());
                            f1.onResult(consumer);
                            cancelTarget.set(f1);
                            return;
                        }
                        catch (final Throwable error) {
                            err = error;
                        }
                    }
                    else {
                        err = r0.error();
                    }
                    consumer.accept(Result.failure(err));
                });
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

    /**
     * Creates new {@code Future} that always succeeds with {@code value}.
     *
     * @param value Value to wrap in {@code Future}.
     * @param <V>   Type of value.
     * @return New {@code Future}.
     */
    static <V> Future<V> success(final V value) {
        return new FutureSuccess<>(value);
    }

    /**
     * Creates new {@code Future} that always fails with {@code error}.
     *
     * @param error Error to wrap in {@code Future}.
     * @param <V>   Type of value that would have been wrapped if successful.
     * @return New {@code Future}.
     */
    static <V> Future<V> failure(final Throwable error) {
        return new FutureFailure<>(error);
    }

    /**
     * Blocks the current thread until the result of given {@code future}
     * becomes available.
     * <p>
     * Note that use of this method is likely to be appropriate only in some
     * rare scenarios, such as when the main thread needs to wait for a server
     * to be shut down or crash.
     * <p>
     * Also note that if calling this method with a future that can only be
     * executed on the current thread after this method has returned, a
     * deadlock occurs.
     *
     * @param future Future to wait for.
     * @param <V>    Type of value provided by {@code future} result, if
     *               successful.
     * @return Result of {@code future}.
     * @throws InterruptedException If the thread waiting for the result to
     *                              become available is interrupted.
     */
    static <V> Result<V> await(final Future<V> future) throws InterruptedException {
        final var result = new AtomicReference<Result<V>>();
        final var isCompleted = new AtomicBoolean();

        future.onResult(result0 -> {
            if (isCompleted.compareAndSet(false, true)) {
                result.set(result0);
            }
        });

        var waiter = new Object();
        while (!isCompleted.compareAndSet(true, false)) {
            waiter.wait();
        }

        return result.getPlain();
    }

    /**
     * Blocks the current thread until the result of all given {@code futures}
     * becomes available.
     * <p>
     * Note that use of this method is likely to be appropriate only in some
     * rare scenarios, such as when the main thread needs to wait for multiple
     * servers to be shut down or crash.
     * <p>
     * Also note that if calling this method with a future that can only be
     * executed on the current thread after this method has returned, a
     * deadlock occurs.
     *
     * @param futures Futures to wait for.
     * @return Results of {@code futures}. The order of the results is the same
     * as the order of the {@code futures} provided to the method.
     * @throws InterruptedException If the thread waiting for the result to
     *                              become available is interrupted.
     */
    static Result<?>[] awaitAll(final Future<?>... futures) throws InterruptedException {
        final var results = new AtomicReferenceArray<Result<?>>(futures.length);
        final var completionCount = new AtomicInteger(0);

        for (var i = 0; i < futures.length; ++i) {
            final var index = i;
            futures[i].onResult(result -> {
                completionCount.getAndIncrement();
                result.ifFailure(error -> results.set(index, result));
            });
        }

        var waiter = new Object();
        while (!completionCount.compareAndSet(futures.length, -1)) {
            waiter.wait();
        }

        final var resultArray = new Result<?>[futures.length];
        for (var i = 0; i < futures.length; ++i) {
            resultArray[i] = results.getPlain(i);
        }
        return resultArray;
    }
}