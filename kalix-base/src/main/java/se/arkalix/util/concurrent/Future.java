package se.arkalix.util.concurrent;

import org.slf4j.LoggerFactory;
import se.arkalix.internal.util.concurrent.NettyThread;
import se.arkalix.util.Result;
import se.arkalix.util.annotation.ThreadSafe;
import se.arkalix.util.function.ThrowingConsumer;
import se.arkalix.util.function.ThrowingFunction;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
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
@SuppressWarnings("unused")
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
     * Sets function to receive result of this {@code Future} only if its
     * operation succeeds. Successful or not, the result is also passed on to
     * the returned {@code Future}.
     * <p>
     * This method is primarily useful for triggering different kinds of side
     * effects that become relevant only if an operation succeeds, such as
     * logging or sending messages.
     * <p>
     * Any exception thrown by {@code consumer} leads to the returned
     * {@code Future} being failed with the same exception.
     *
     * @param consumer Function invoked if this {@code Future} completes
     *                 successfully.
     * @return New {@code Future} completed with the result of this {@code
     * Future}.
     * @throws NullPointerException If {@code consumer} is {@code null}.
     */
    default Future<V> ifSuccess(final ThrowingConsumer<? super V> consumer) {
        Objects.requireNonNull(consumer, "Expected consumer");
        final var source = this;
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<V>> consumer0) {
                source.onResult(result0 -> {
                    Result<V> result1;
                    try {
                        if (result0.isSuccess()) {
                            consumer.accept(result0.value());
                        }
                        result1 = result0;
                    }
                    catch (final Throwable throwable) {
                        result1 = Result.failure(throwable);
                    }
                    consumer0.accept(result1);
                });
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                source.cancel(mayInterruptIfRunning);
            }
        };
    }

    /**
     * Sets function to receive result of this {@code Future} only if its
     * operation fails. Successful or not, the result is also passed on to
     * the returned {@code Future}.
     * <p>
     * This method is primarily useful for triggering different kinds of side
     * effects that become relevant only if an operation fails, such as logging
     * or sending messages.
     * <p>
     * Any exception thrown by {@code consumer} leads to the returned
     * {@code Future} being failed with the same exception. The exception that
     * caused the {@code consumer} function to be called is added as a
     * suppressed exception to the new exception thrown by the {@code consumer}
     * function.
     *
     * @param consumer Function invoked if this {@code Future} completes with
     *                 an fault.
     * @return New {@code Future} completed with the result of this {@code
     * Future}.
     * @throws NullPointerException If {@code consumer} is {@code null}.
     */
    default <T extends Throwable> Future<V> ifFailure(final Class<T> class_, final ThrowingConsumer<T> consumer) {
        Objects.requireNonNull(consumer, "Expected consumer");
        final var source = this;
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<V>> consumer0) {
                source.onResult(result0 -> {
                    Result<V> result1;
                    try {
                        if (result0.isFailure()) {
                            final var fault = result0.fault();
                            if (class_.isAssignableFrom(fault.getClass())) {
                                consumer.accept(class_.cast(result0.fault()));
                            }
                        }
                        result1 = result0;
                    }
                    catch (final Throwable throwable) {
                        throwable.addSuppressed(result0.fault());
                        result1 = Result.failure(throwable);
                    }
                    consumer0.accept(result1);
                });
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                source.cancel(mayInterruptIfRunning);
            }
        };
    }

    /**
     * Sets function to receive result of this {@code Future}, no matter if it
     * succeeds or not. The result is also passed on to the returned
     * {@code Future}.
     * <p>
     * This method is primarily useful for triggering different kinds of side
     * effects, such as logging or sending messages.
     * <p>
     * Any exception thrown by {@code consumer} leads to the returned
     * {@code Future} being failed with the same exception. If the result of
     * this {@code Future} is a fault, that fault is added as a suppressed
     * exception to the new exception thrown by the {@code consumer} function.
     *
     * @param consumer Function invoked with the result of this {@code Future}.
     * @return New {@code Future} completed with the result of this {@code
     * Future}.
     * @throws NullPointerException If {@code consumer} is {@code null}.
     */
    default Future<V> always(final ThrowingConsumer<Result<? super V>> consumer) {
        Objects.requireNonNull(consumer, "Expected consumer");
        final var source = this;
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<V>> consumer0) {
                source.onResult(result0 -> {
                    Result<V> result1;
                    try {
                        consumer.accept(result0);
                        result1 = result0;
                    }
                    catch (final Throwable throwable) {
                        if (result0.isFailure()) {
                            throwable.addSuppressed(result0.fault());
                        }
                        result1 = Result.failure(throwable);
                    }
                    consumer0.accept(result1);
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
     * @throws NullPointerException If {@code mapper} is {@code null}.
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
     * Catches any fault produced by this {@code Future} that is assignable to
     * {@code class_} and uses {@code mapper} to transform it into a new value.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     try {
     *         return originalFutureOperation();
     *     }
     *     catch (final U throwable) {
     *         return mapper.apply(throwable);
     *     }
     * </pre>
     * Any exception thrown by {@code mapper} leads to the returned
     * {@code Future} being failed with the same exception.
     *
     * @param class_ Class caught exceptions must be assignable to.
     * @param mapper The mapping function to apply to the fault of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the fault of this {@code Future}.
     * @throws NullPointerException If {@code class_} or {@code mapper} is
     *                              {@code null}.
     */
    default <T extends Throwable> Future<V> mapCatch(
        final Class<T> class_,
        final ThrowingFunction<T, ? extends V> mapper)
    {
        Objects.requireNonNull(class_, "Expected class_");
        Objects.requireNonNull(mapper, "Expected mapper");
        final var source = this;
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                source.onResult(result0 -> {
                    Result<V> result1;
                    result:
                    if (result0.isSuccess()) {
                        result1 = result0;
                    }
                    else {
                        var fault = result0.fault();
                        if (class_.isAssignableFrom(fault.getClass())) {
                            try {
                                result1 = Result.success(mapper.apply(class_.cast(fault)));
                                break result;
                            }
                            catch (final Throwable throwable) {
                                fault = throwable;
                            }
                        }
                        result1 = Result.failure(fault);
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
     * Applies any fault produced by this {@code Future} that is assignable to
     * {@code class_} to {@code mapper} and then fails the returned future
     * with the {@code Throwable} it returns.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     try {
     *         return originalFutureOperation();
     *     }
     *     catch (final T throwable) {
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
     * @throws NullPointerException If {@code class_} or {@code mapper} is
     *                              {@code null}.
     */
    default <T extends Throwable> Future<V> mapFault(
        final Class<T> class_,
        final ThrowingFunction<Throwable, Throwable> mapper)
    {
        Objects.requireNonNull(class_, "Expected class_");
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
                        Throwable cause1 = result0.fault();
                        if (class_.isAssignableFrom(cause1.getClass())) {
                            try {
                                cause1 = mapper.apply(result0.fault());
                            }
                            catch (final Throwable throwable) {
                                cause1 = throwable;
                            }
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
     * @throws NullPointerException If {@code mapper} is {@code null}.
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
     * Takes any successful result produced by this {@code Future} and uses
     * {@code mapper} to transform it into a fault.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     V value = originalFutureOperation();
     *     // Wait for value to become available.
     *     Throwable fault = mapper.apply(value);
     *     throw fault;
     * </pre>
     * Any exception thrown by {@code mapper} leads to the returned
     * {@code Future} being failed with the same exception.
     *
     * @param mapper The mapping function to apply to the value of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}.
     * @throws NullPointerException If {@code mapper} is {@code null}.
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
     * @throws NullPointerException If {@code mapper} is {@code null}.
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
     * Catches any fault produced by this {@code Future} that is assignable to
     * {@code class_} and uses {@code mapper} to transform it into a new value.
     * <p>
     * The difference between this method and
     * {@link #mapCatch(Class, ThrowingFunction)} is that this method expects
     * its {@code mapper} to return a {@code Future} rather than a plain value.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     try {
     *         return originalFutureOperation();
     *     }
     *     catch (final U throwable) {
     *         V value = mapper.apply(throwable);
     *         // Wait for new value to become available.
     *         return value;
     *     }
     * </pre>
     * Any exception thrown by {@code mapper} leads to the returned
     * {@code Future} being failed with the same exception.
     *
     * @param class_ Class that caught exceptions must be assignable to.
     * @param mapper The mapping function to apply to the fault of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the fault of this {@code Future}, and then waiting
     * for the {@code Future} returned by the mapper to complete.
     * @throws NullPointerException If {@code class_} or {@code mapper} is
     *                              {@code null}.
     */
    default <T extends Throwable> Future<V> flatMapCatch(
        final Class<T> class_,
        final ThrowingFunction<T, ? extends Future<V>> mapper)
    {
        Objects.requireNonNull(class_, "Expected class_");
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
                        var fault = result0.fault();
                        if (class_.isAssignableFrom(fault.getClass())) {
                            try {
                                final var future1 = mapper.apply(class_.cast(fault));
                                future1.onResult(consumer);
                                cancelTarget = future1;
                                return;
                            }
                            catch (final Throwable throwable) {
                                fault = throwable;
                            }
                        }
                        else {
                            fault = result0.fault();
                        }
                        result1 = Result.failure(fault);
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
     * {@link #mapFault(Class, ThrowingFunction)} is that this method expects
     * its {@code mapper} to return a {@code Future<Throwable>} rather than a
     * plain {@code Throwable}.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     try {
     *         return originalFutureOperation();
     *     }
     *     catch (final T throwable) {
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
     * @throws NullPointerException If {@code class_} or {@code mapper} is
     *                              {@code null}.
     */
    default <T extends Throwable> Future<V> flatMapFault(
        final Class<T> class_,
        final ThrowingFunction<Throwable, ? extends Future<Throwable>> mapper)
    {
        Objects.requireNonNull(class_, "Expected class_");
        Objects.requireNonNull(mapper, "Expected mapper");
        final var source = this;
        return new Future<>() {
            private Future<?> cancelTarget = source;

            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                source.onResult(result0 -> {
                    Result<V> result1;
                    if (cancelTarget == null) {
                        return;
                    }
                    if (result0.isSuccess()) {
                        result1 = result0;
                    }
                    else {
                        Throwable cause1 = result0.fault();
                        if (class_.isAssignableFrom(cause1.getClass())) {
                            try {
                                final var future1 = mapper.apply(cause1);
                                future1.onResult(result -> consumer.accept(Result.failure(result.isSuccess()
                                    ? result.value()
                                    : result.fault())));
                                cancelTarget = future1;
                                return;
                            }
                            catch (final Throwable throwable) {
                                cause1 = throwable;
                            }
                        }
                        result1 = Result.failure(cause1);
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
     * @throws NullPointerException If {@code mapper} is {@code null}.
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
     * Applies any value successfully produced by this {@code Future} to
     * {@code mapper}, and then fails the returned future with the
     * {@code Throwable} it returns.
     * <p>
     * The difference between this method and
     * {@link #mapThrow(ThrowingFunction)} is that this method expects
     * its {@code mapper} to return a {@code Future<Throwable>} rather than a
     * plain {@code Throwable}.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *         V value = originalFutureOperation();
     *         // Wait for value to become available.
     *         Throwable fault = mapper.apply(value);
     *         // Wait for fault to become available.
     *         throw fault;
     *     }
     * </pre>
     * Any exception thrown by {@code mapper} leads to the returned
     * {@code Future} being failed with the same exception. Furthermore, if
     * the {@code Future} returned by mapper fails, the {@code Future} returned
     * by this method is failed with that exception.
     *
     * @param mapper The mapping function to apply to the value of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}, and then waiting
     * for the {@code Future} returned by the mapper to complete.
     * @throws NullPointerException If {@code mapper} is {@code null}.
     */
    default Future<V> flatMapThrow(final ThrowingFunction<V, ? extends Future<? extends Throwable>> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        final var source = this;
        return new Future<>() {
            private Future<?> cancelTarget = source;

            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                source.onResult(result0 -> {
                    Result<V> result1;
                    if (cancelTarget == null) {
                        return;
                    }
                    if (result0.isFailure()) {
                        result1 = result0;
                    }
                    else {
                        try {
                            final var future1 = mapper.apply(result0.value());
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
     * Returns new {@code Future} that succeeds with given {@code value} if
     * this {@code Future} completes successfully.
     * <p>
     * If this {@code Future} fails, on the other hand, the returned
     * {@code Future} is failed with the same fault.
     *
     * @param value Value to include in the returned {@code Future}.
     * @return A {@code Future} that will complete with given {@code throwable}
     * as fault.
     */
    default <U> Future<U> pass(final U value) {
        final var source = this;
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<U>> consumer) {
                source.onResult(result -> {
                    if (result.isSuccess()) {
                        consumer.accept(Result.success(value));
                    }
                    else {
                        consumer.accept(Result.failure(result.fault()));
                    }
                });
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                source.cancel(mayInterruptIfRunning);
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
     * @throws NullPointerException If {@code throwable} is {@code null}.
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
     * Converts this {@code Future} into a {@link FutureAnnouncement}, which
     * allows its result to be received by any number of subscribers.
     * <p>
     * As noted {@link #onResult(Consumer) here}, a {@code Future} should only
     * ever present its result to a single receiver. This method allows for
     * that scope be widened to zero or more receivers.
     *
     * @return {@code FutureAnnouncement} advertising the result of this
     * {@code Future} to any number of subscribers.
     */
    default FutureAnnouncement<V> toAnnouncement() {
        return new FutureAnnouncement<>(this);
    }

    /**
     * Delays the result of this {@code Future} from the time it becomes
     * available.
     *
     * @param duration Duration to delay the completion of this {@code Future}
     *                 with.
     * @return New {@code Future} that eventually completes with the result of
     * this {@code Future}.
     */
    default Future<V> delay(final Duration duration) {
        Objects.requireNonNull(duration, "Expected duration");
        final var source = this;
        return new Future<>() {
            private Future<?> cancelTarget = source;

            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                source.onResult(result -> {
                    if (cancelTarget != null) {
                        cancelTarget = Schedulers.fixed()
                            .schedule(duration, () -> consumer.accept(result));
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
     * Creates new {@code Future} that delays its completion until right after
     * the given {@code baseline}.
     * <p>
     * In other words, if this {@code Future} completes after the {@code
     * baseline}, its result is passed on immediately by the returned {@code
     * Future}. If, on the other hand, this {@code Future} completes before the
     * {@code baseline}, the returned {@code Future} is scheduled for
     * completion at some point right after that {@code baseline}.
     *
     * @param baseline Instant to delay result until, unless the result becomes
     *                 available after that instant.
     * @return New {@code Future} that eventually completes with the result of
     * this {@code Future}.
     */
    default Future<V> delayUntil(final Instant baseline) {
        Objects.requireNonNull(baseline, "Expected baseline");
        final var source = this;
        return new Future<>() {
            private Future<?> cancelTarget = source;

            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                source.onResult(result -> {
                    if (cancelTarget != null) {
                        final var duration = Duration.between(baseline, Instant.now());
                        if (duration.isNegative() || duration.isZero()) {
                            consumer.accept(result);
                        }
                        else {
                            cancelTarget = Schedulers.fixed()
                                .schedule(duration, () -> consumer.accept(result));
                        }
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
     * Returns new {@code Future} that is completed successfully only if this
     * {@code Future} completes successfully and its result can be provided to
     * the given {@code consumer} function <i>on a separate thread</i>.
     * <p>
     * This method exists primarily as a means of helping prevent blocking
     * calls from stalling threads in the {@link Schedulers#fixed() default
     * fixed scheduler}, which has only a fixed number of threads. It should be
     * used for long-running computations, blocking I/O, blocking database
     * operations and other related kinds of use cases, but only when the
     * result of the blocking operation will not be needed on the original
     * thread from which it was forked. In that case, rather use the
     * {@link #forkJoin(ThrowingFunction) forkJoin} method.
     * <p>
     * It is the responsibility of the caller not to make any calls or execute
     * any operations that may lead to race conditions. Note that only those
     * Kalix library methods that are explicitly designated as thread-safe,
     * using the {@link ThreadSafe @ThreadSafe} annotation, can be safely
     * called without explicit synchronization when accessed by multiple
     * threads.
     * <p>
     * Any exception thrown by {@code consumer} has no impact on the returned
     * {@code Future}, but will be logged.
     *
     * @param consumer Function to invoke with successful result of this
     *                 {@code Future}, if it ever becomes available.
     * @return A {@code Future} that will be completed either with the fault of
     * this {@code Future} or with {@code null} if the given {@code consumer}
     * function could be executed on a separate thread.
     */
    default Future<?> fork(final Consumer<V> consumer) {
        Objects.requireNonNull(consumer, "Expected consumer");
        final var source = this;
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<Object>> consumer0) {
                source.onResult(result0 -> {
                    Result<Object> result1;
                    success:
                    {
                        Throwable cause;
                        if (result0.isSuccess()) {
                            try {
                                Schedulers.dynamic().execute(() -> {
                                    try {
                                        consumer.accept(result0.value());
                                    }
                                    catch (final Throwable throwable) {
                                        LoggerFactory.getLogger(Future.class)
                                            .error("Unexpected fork consumer exception caught", throwable);
                                    }
                                });
                                result1 = Result.done();
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
                    consumer0.accept(result1);
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
     * value of type {@code U} by {@code mapper} on a separate thread.
     * <p>
     * In other words, this method performs exactly the same operation as the
     * {@link #map(ThrowingFunction) map} method, <i>but performs its execution
     * on a separate thread</i>. When computation finishes, the result is
     * returned to the original thread and the returned future is completed.
     * If, however, the thread completing this {@code Future} is <i>not</i>
     * owned by the {@link Schedulers#fixed() default fixed scheduler},
     * <i>this method will fail</i>. This as the current thread is then not
     * associated with a scheduler that can be accessed to schedule the joining
     * of the fork without blocking.
     * <p>
     * Unless otherwise noted, all Kalix methods returning {@code Futures} will
     * always use the default fixed scheduler, which means that this method is
     * then safe to use.
     * <p>
     * This method exists primarily as a means of helping prevent blocking
     * calls from stalling threads in the {@link Schedulers#fixed() default
     * fixed scheduler}, which has only a fixed number of threads. It should be
     * used for long-running computations, blocking I/O, blocking database
     * operations and other related kinds of use cases.
     * <p>
     * It is the responsibility of the caller not to make any calls or execute
     * any operations that may lead to race conditions. Note that only those
     * Kalix library methods that are explicitly designated as thread-safe,
     * using the {@link ThreadSafe @ThreadSafe} annotation, can be safely
     * called without explicit synchronization when accessed by multiple
     * threads.
     * <p>
     * Any exception thrown by {@code mapper} leads to the returned
     * {@code Future} being failed with the same exception.
     *
     * @param <U>    The type of the value returned from the mapping function.
     * @param mapper The mapping function to apply to the value of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}, if it completes
     * successfully.
     * @throws NullPointerException If {@code mapper} is {@code null}.
     */
    default <U> Future<U> forkJoin(final ThrowingFunction<V, U> mapper) {
        Objects.requireNonNull(mapper, "Expected mapper");
        final var source = this;
        return new Future<>() {
            private AtomicReference<Future<?>> cancelTarget = new AtomicReference<>(source);

            @Override
            public void onResult(final Consumer<Result<U>> consumer) {
                source.onResult(result0 -> {
                    if (cancelTarget.get() == null) {
                        return;
                    }
                    Throwable fault1;
                    fault:
                    {
                        if (result0.isFailure()) {
                            fault1 = result0.fault();
                            break fault;
                        }

                        final var thread = Thread.currentThread();
                        if (!(thread instanceof NettyThread)) {
                            fault1 = new IllegalStateException("Result not " +
                                "provided by default fixed scheduler thread; " +
                                "joining fork would not be possible");
                            break fault;
                        }

                        final var eventLoop = ((NettyThread) thread).eventLoop();
                        if (eventLoop == null || !eventLoop.inEventLoop()) {
                            fault1 = new IllegalStateException("Current " +
                                "thread not associated with a task queue; " +
                                "joining fork would not be possible");
                            break fault;
                        }

                        final var scheduler = Schedulers.dynamic();
                        final var future1 = scheduler.submit(() -> {
                            if (cancelTarget.get() == null) {
                                return;
                            }
                            Result<U> result1;
                            try {
                                result1 = Result.success(mapper.apply(result0.value()));
                            }
                            catch (final Throwable throwable) {
                                result1 = Result.failure(throwable);
                            }
                            final var result2 = result1;
                            try {
                                eventLoop.execute(() -> consumer.accept(result2));
                            }
                            catch (final Throwable throwable) {
                                if (throwable instanceof RejectedExecutionException && eventLoop.isShuttingDown()) {
                                    return;
                                }
                                LoggerFactory.getLogger(Future.class)
                                    .error("Failed to join fork", throwable);
                            }
                        });
                        cancelTarget.set(future1);
                        return;
                    }
                    consumer.accept(Result.failure(fault1));
                });
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                final var cancelTarget0 = cancelTarget.getAndSet(null);
                if (cancelTarget0 != null) {
                    cancelTarget0.cancel(mayInterruptIfRunning);
                }
            }
        };
    }

    /**
     * Returns {@code Future} that always succeeds with {@code null}.
     *
     * @return Cached {@code Future}.
     */
    @SuppressWarnings("unchecked")
    @ThreadSafe
    static <V> Future<V> done() {
        return (Future<V>) FutureSuccess.NULL;
    }

    /**
     * Creates new {@code Future} that always succeeds with {@code value}.
     *
     * @param value Value to wrap in {@code Future}.
     * @param <V>   Type of value.
     * @return New {@code Future}.
     */
    @ThreadSafe
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
    @ThreadSafe
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
    @ThreadSafe
    static <V> Future<V> of(final Result<V> result) {
        return new FutureResult<>(result);
    }
}