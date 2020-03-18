package eu.arrowhead.kalix.util.concurrent;

import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.function.ThrowingFunction;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

/**
 * Represents an operation that will complete at some point in the future.
 * <p>
 * To make it convenient to act on the completion of this {@code Future}, the
 * receiver of it is expected to provide a {@code Consumer} to the
 * {@link #onResult(Consumer)} method, which should be invoked whenever the
 * {@link Result} of the operation becomes available.
 * <p>
 * It is the responsibility of the receiver of a {@code Future} to make sure
 * that its {@link #onResult(Consumer)} is called, or any method listed here
 * that uses it internally, as it may be the case that the operation
 * represented by the future will not start running this method is invoked.
 * Failing to call it may lead to memory or other resources never being
 * reclaimed.
 * <p>
 * Furthermore, implementations of this interface are likely <i>not</i> going
 * to be thread safe. Unless otherwise advertised, sharing individual
 * {@code Futures} between multiple threads may cause race conditions.
 *
 * @param <V> Type of value that can be retrieved if the operation succeeds.
 */
public interface Future<V> {
    /**
     * Sets function to receive result of this {@code Future}, when and if it
     * becomes available.
     * <p>
     * If this method has been called previously on the same {@code Future},
     * any previously set consumer should be replaced by the one given. If a
     * previous consumer has already been called when a new one is set, the new
     * one <i>might</i> be called with the same result as the previous.
     * <p>
     * The given consumer function should never throw exceptions that need to
     * be handled explicitly. Any thrown exception will end up with the caller
     * of the {@code consumer}, which is unlikely to be able to handle it in
     * any other way than by logging it.
     *
     * @param consumer Function invoked when this {@code Future} completes.
     * @throws NullPointerException If {@code consumer} is {@code null}.
     */
    void onResult(final Consumer<Result<V>> consumer);

    /**
     * Signals that the result of this {@code Future} no longer is of interest.
     * <p>
     * No guarantees whatsoever are given about the implications of this call.
     * It may prevent {@link #onResult(Consumer)} from being called, or may
     * cause it to be called with a {@link CancellationException}, or something
     * else entirely. However, the receiver of a {@code Future} that is no
     * longer interested in its successful result <i>should</i> call this
     * method to make it clear to the original issuer of the {@code Future},
     * unless a result has already been received.
     *
     * @param mayInterruptIfRunning Whether or not the thread executing the
     *                              task associated with this {@code Future},
     *                              if any, should be interrupted. If not,
     *                              in-progress tasks are allowed to complete.
     *                              This parameter may be ignored.
     */
    void cancel(final boolean mayInterruptIfRunning);

    /**
     * Signals that the result of this {@code Future} no longer is of interest
     * and that evaluation of the {@code Future} should be gracefully
     * terminated.
     * <p>
     * No guarantees whatsoever are given about the implications of this call.
     * It may prevent {@link #onResult(Consumer)} from being called, or may
     * cause it to be called with a {@link CancellationException}, or something
     * else entirely. However, the receiver of a {@code Future} that is no
     * longer interested in its successful result <i>should</i> call this
     * method to make it clear to the original issuer of the {@code Future},
     * unless a result has already been received.
     */
    default void cancel() {
        cancel(false);
    }

    /**
     * Sets function to receive result of this {@code Future} only if its
     * operation fails. Successful results are ignored.
     * <p>
     * While it might seem like a logical addition, there is no corresponding
     * {@code #onValue(Consumer)} method. The reason for this is that there is
     * no reasonable default strategy for handling ignored faults. Silently
     * discarding them is unlikely to be a suitable design choice, as it could
     * hide details important for discovering or tracking application issues,
     * and logging them would necessitate information about the context in
     * which the successful result was required, as well as integration against
     * a logging framework.
     *
     * @param consumer Function invoked if this {@code Future} completes with
     *                 an fault.
     * @throws NullPointerException If {@code consumer} is {@code null}.
     */
    default void onFailure(final Consumer<Throwable> consumer) {
        Objects.requireNonNull(consumer, "Expected consumer");
        onResult(result -> {
            if (result.isFailure()) {
                consumer.accept(result.fault());
            }
        });
    }

    /**
     * Returns new {@code Future} that is completed after the value of this
     * {@code Future} has become available and could be transformed into a
     * value of type {@code U} by {@code mapper}.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     V value0 = originalFutureOperation();
     *     // Wait for value0 to become available.
     *     U value1 = mapper.apply(value0);
     *     return value1;
     * </pre>
     * Any exception thrown by {@code mapper} leads to the returned
     * {@code Future} being failed with the same exception.
     *
     * @param <U>    The type of the value returned from the mapping function.
     * @param mapper The mapping function to apply to the value of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}, if it completes
     * successfully.
     * @throws NullPointerException If the mapping function is {@code null}.
     */
    default <U> Future<U> map(final ThrowingFunction<? super V, U> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        final var source = this;
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<U>> consumer) {
                source.onResult(result0 -> {
                    Result<U> result1;
                    success:
                    {
                        Throwable cause;
                        if (result0.isSuccess()) {
                            try {
                                result1 = Result.success(mapper.apply(result0.value()));
                                break success;
                            }
                            catch (final Throwable throwable) {
                                cause = throwable;
                            }
                        }
                        else {
                            cause = result0.fault();
                        }
                        result1 = Result.failure(cause);
                    }
                    consumer.accept(result1);
                });
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                source.cancel(mayInterruptIfRunning);
            }
        };
    }

    /**
     * Catches any fault produced by this {@code Future} and uses
     * {@code mapper} to transform it into a new value.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     try {
     *         return originalFutureOperation();
     *     }
     *     catch (final Throwable throwable) {
     *         return mapper.apply(throwable);
     *     }
     * </pre>
     * Any exception thrown by {@code mapper} leads to the returned
     * {@code Future} being failed with the same exception.
     *
     * @param mapper The mapping function to apply to the fault of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the fault of this {@code Future}.
     * @throws NullPointerException If the mapping function is {@code null}.
     */
    default Future<V> mapCatch(final ThrowingFunction<Throwable, ? extends V> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        final var source = this;
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                source.onResult(result0 -> {
                    Result<V> result1;
                    if (result0.isSuccess()) {
                        result1 = result0;
                    }
                    else {
                        try {
                            result1 = Result.success(mapper.apply(result0.fault()));
                        }
                        catch (final Throwable throwable) {
                            result1 = Result.failure(throwable);
                        }
                    }
                    consumer.accept(result1);
                });
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                source.cancel(mayInterruptIfRunning);
            }
        };
    }

    /**
     * Takes any successful result produced by this {@code Future} and uses
     * {@code mapper} to transform it into a fault.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     V value = originalFutureOperation();
     *     // Wait for value to become available.
     *     Throwable fault = mapper.apply(value);
     *     return fault;
     * </pre>
     * Any exception thrown by {@code mapper} leads to the returned
     * {@code Future} being failed with the same exception.
     *
     * @param mapper The mapping function to apply to the value of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}.
     * @throws NullPointerException If the mapping function is {@code null}.
     */
    default <U> Future<U> mapThrow(final ThrowingFunction<? super V, Throwable> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        final var source = this;
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<U>> consumer) {
                source.onResult(result0 -> {
                    Throwable cause;
                    if (result0.isSuccess()) {
                        try {
                            cause = mapper.apply(result0.value());
                        }
                        catch (final Throwable throwable) {
                            cause = throwable;
                        }
                    }
                    else {
                        cause = result0.fault();
                    }
                    consumer.accept(Result.failure(cause));
                });
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                source.cancel(mayInterruptIfRunning);
            }
        };
    }

    /**
     * Applies any fault produced by this {@code Future} to {@code mapper} and
     * then fails the returned future with the {@code Throwable} it returns.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     try {
     *         return originalFutureOperation();
     *     }
     *     catch (final Throwable throwable) {
     *         throw mapper.apply(throwable);
     *     }
     * </pre>
     * Any exception thrown by {@code mapper} leads to the returned
     * {@code Future} being failed with the same exception, meaning it is
     * functionally equivalent to returning the exception.
     *
     * @param mapper The mapping function to apply to the fault of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the fault of this {@code Future}.
     * @throws NullPointerException If the mapping function is {@code null}.
     */
    default Future<V> mapFault(final ThrowingFunction<Throwable, Throwable> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        final var source = this;
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                source.onResult(result0 -> {
                    Result<V> result1;
                    if (result0.isSuccess()) {
                        result1 = result0;
                    }
                    else {
                        Throwable cause1;
                        try {
                            cause1 = mapper.apply(result0.fault());
                        }
                        catch (final Throwable throwable) {
                            cause1 = throwable;
                        }
                        result1 = Result.failure(cause1);
                    }
                    consumer.accept(result1);
                });
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                source.cancel(mayInterruptIfRunning);
            }
        };
    }

    /**
     * Returns new {@code Future} that is completed after the value of this
     * {@code Future} has become available and could be transformed into a
     * result of type {@code Result<U>} by {@code mapper}.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     Result&lt;V&gt; result0 = originalFutureOperation();
     *     // Wait for result0 to become available.
     *     Result&lt;U&gt; result1 = mapper.apply(result0);
     *     return result1;
     * </pre>
     * Any exception thrown by {@code mapper} leads to the returned
     * {@code Future} being failed with the same exception.
     *
     * @param <U>    The type of the value returned from the mapping function.
     * @param mapper The mapping function to apply to the value of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}, if it completes
     * successfully.
     * @throws NullPointerException If the mapping function is {@code null}.
     */
    default <U> Future<U> mapResult(final ThrowingFunction<Result<V>, Result<U>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        final var source = this;
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<U>> consumer) {
                source.onResult(result0 -> {
                    Result<U> result1;
                    try {
                        result1 = mapper.apply(result0);

                    }
                    catch (final Throwable throwable) {
                        result1 = Result.failure(throwable);
                    }
                    consumer.accept(result1);
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
     * The difference between this method and {@link #map(ThrowingFunction)} is
     * that the {@code mapper} provided here is expected to return a
     * {@code Future} rather than a plain value. The returned {@code Future}
     * completes after this {@code Future} and the {@code Future} returned by
     * {@code mapper} have completed in sequence.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     V value0 = originalFutureOperation();
     *     // Wait for value0 to become available.
     *     U value1 = mapper.apply(value0);
     *     // Wait for value1 to become available.
     *     return value1;
     * </pre>
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
        Objects.requireNonNull(mapper, "Expected mapper");
        final var source = this;
        return new Future<>() {
            private Future<?> cancelTarget = source;

            @Override
            public void onResult(final Consumer<Result<U>> consumer) {
                source.onResult(result0 -> {
                    if (cancelTarget == null) {
                        return;
                    }
                    Throwable cause;
                    if (result0.isSuccess()) {
                        try {
                            final var future1 = mapper.apply(result0.value());
                            future1.onResult(consumer);
                            cancelTarget = future1;
                            return;
                        }
                        catch (final Throwable throwable) {
                            cause = throwable;
                        }
                    }
                    else {
                        cause = result0.fault();
                    }
                    consumer.accept(Result.failure(cause));
                });
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

    /**
     * Catches any fault produced by this {@code Future} and uses
     * {@code mapper} to transform it into a new value.
     * <p>
     * The difference between this method and
     * {@link #mapCatch(ThrowingFunction)} is that this method expects its
     * {@code mapper} to return a {@code Future} rather than a plain value.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     try {
     *         return originalFutureOperation();
     *     }
     *     catch (final Throwable throwable) {
     *         V value = mapper.apply(throwable);
     *         // Wait for new value to become available.
     *         return value;
     *     }
     * </pre>
     * Any exception thrown by {@code mapper} leads to the returned
     * {@code Future} being failed with the same exception.
     *
     * @param mapper The mapping function to apply to the fault of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the fault of this {@code Future}, and then waiting
     * for the {@code Future} returned by the mapper to complete.
     * @throws NullPointerException If the mapping function is {@code null}.
     */
    default Future<V> flatMapCatch(final ThrowingFunction<Throwable, ? extends Future<V>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        final var source = this;
        return new Future<>() {
            private Future<?> cancelTarget = source;

            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                source.onResult(result0 -> {
                    Result<V> result1;
                    done:
                    {
                        if (cancelTarget == null) {
                            return;
                        }
                        else {
                            if (result0.isSuccess()) {
                                result1 = result0;
                                break done;
                            }
                            try {
                                final var future1 = mapper.apply(result0.fault());
                                future1.onResult(consumer);
                                cancelTarget = future1;
                                return;
                            }
                            catch (final Throwable throwable) {
                                result1 = Result.failure(throwable);
                            }
                        }
                    }
                    consumer.accept(result1);
                });
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

    /**
     * Applies any fault produced by this {@code Future} to {@code mapper}, and
     * then fails the returned future with the {@code Throwable} it returns.
     * <p>
     * The difference between this method and
     * {@link #mapFault(ThrowingFunction)} is that this method expects its
     * {@code mapper} to return a {@code Future<Throwable>} rather than a plain
     * {@code Throwable}.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     try {
     *         return originalFutureOperation();
     *     }
     *     catch (final Throwable throwable) {
     *         Throwable fault = mapper.apply(throwable);
     *         // Wait for fault to become available.
     *         throw fault;
     *     }
     * </pre>
     * Any exception thrown by {@code mapper} leads to the returned
     * {@code Future} being failed with the same exception. Furthermore, if
     * the {@code Future} returned by mapper fails, the {@code Future} returned
     * by this method is failed with that exception.
     *
     * @param mapper The mapping function to apply to the fault of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the fault of this {@code Future}, and then waiting
     * for the {@code Future} returned by the mapper to complete.
     * @throws NullPointerException If the mapping function is {@code null}.
     */
    default Future<V> flatMapFault(final ThrowingFunction<Throwable, ? extends Future<Throwable>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        final var source = this;
        return new Future<>() {
            private Future<?> cancelTarget = source;

            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                source.onResult(result0 -> {
                    Result<V> result1;
                    done:
                    {
                        if (cancelTarget == null) {
                            return;
                        }
                        if (result0.isSuccess()) {
                            result1 = result0;
                            break done;
                        }
                        try {
                            final var future1 = mapper.apply(result0.fault());
                            future1.onResult(result -> consumer.accept(Result.failure(result.isSuccess()
                                ? result.value()
                                : result.fault())));
                            cancelTarget = future1;
                            return;
                        }
                        catch (final Throwable throwable) {
                            result1 = Result.failure(throwable);
                        }

                    }
                    consumer.accept(result1);
                });
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

    /**
     * Returns new {@code Future} that is completed when the {@code Future}
     * returned by {@code mapper} completes, which, in turn, is not executed
     * until this {@code Future} completes.
     * <p>
     * The difference between this method and
     * {@link #mapResult(ThrowingFunction)} is that the {@code mapper} provided
     * here is expected to return a {@code Future} rather than a plain result.
     * The returned {@code Future} completes after this {@code Future} and the
     * {@code Future} returned by {@code mapper} have completed in sequence.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     Result&lt;V&gt; result0 = originalFutureOperation();
     *     // Wait for result0 to become available.
     *     U value1 = mapper.apply(result0);
     *     // Wait for value1 to become available.
     *     return value1;
     * </pre>
     * Any exception thrown by {@code mapper} should lead to the returned
     * future being failed with the same exception.
     *
     * @param <U>    The type of the value returned from the mapping function.
     * @param mapper The mapping function to apply to the value of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the result of this {@code Future}, and then waiting
     * for the {@code Future} returned by the mapper to complete.
     * @throws NullPointerException If the mapping function is {@code null}.
     */
    default <U> Future<U> flatMapResult(final ThrowingFunction<Result<V>, ? extends Future<U>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        final var source = this;
        return new Future<>() {
            private Future<?> cancelTarget = source;

            @Override
            public void onResult(final Consumer<Result<U>> consumer) {
                source.onResult(result0 -> {
                    if (cancelTarget == null) {
                        return;
                    }
                    try {
                        final var future1 = mapper.apply(result0);
                        future1.onResult(consumer);
                        cancelTarget = future1;
                    }
                    catch (final Throwable throwable) {
                        consumer.accept(Result.failure(throwable));
                    }
                });
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

    /**
     * Returns new {@code Future} that is guaranteed to fail with given
     * {@code throwable}.
     * <p>
     * If this {@code Future} fails, its fault is added as a suppressed
     * exception to the provided {@code throwable}. If this {@code Future}
     * succeeds, its result is ignored.
     *
     * @param throwable Fault to include in returned {@code Future}.
     * @return A {@code Future} that will complete with given {@code throwable}
     * as fault.
     */
    default <U> Future<U> fail(final Throwable throwable) {
        Objects.requireNonNull(throwable, "Expected throwable");
        final var source = this;
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<U>> consumer) {
                source.onResult(result -> {
                    if (result.isFailure()) {
                        throwable.addSuppressed(result.fault());
                    }
                    consumer.accept(Result.failure(throwable));
                });
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                source.cancel(mayInterruptIfRunning);
            }
        };
    }

    /**
     * Returns {@code Future} that always succeeds with {@code null}.
     *
     * @return Cached {@code Future}.
     */
    static Future<?> done() {
        return FutureSuccess.NULL;
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
     * Creates new {@code Future} that always fails with {@code fault}.
     *
     * @param fault Error to wrap in {@code Future}.
     * @param <V>   Type of value that would have been wrapped if successful.
     * @return New {@code Future}.
     */
    static <V> Future<V> failure(final Throwable fault) {
        return new FutureFailure<>(fault);
    }

    /**
     * Creates new {@code Future} that always completes with {@code result}.
     *
     * @param result Result to wrap in {@code Future}.
     * @param <V>    Type of value that is being wrapped if {@code result} is
     *               successful.
     * @return New {@code Future}.
     */
    static <V> Future<V> of(final Result<V> result) {
        return new FutureResult<>(result);
    }
}