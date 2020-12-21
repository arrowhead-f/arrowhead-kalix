package se.arkalix.util.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.util.Result;
import se.arkalix.util.annotation.ThreadSafe;
import se.arkalix.util.concurrent._internal.FutureConsumption;
import se.arkalix.util.concurrent._internal.FutureConsumptionWithExtraCancelTarget;
import se.arkalix.util.concurrent._internal.NettyThread;
import se.arkalix.util.function.ThrowingConsumer;
import se.arkalix.util.function.ThrowingFunction;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Represents an operation that will complete at some point in the future.
 * <p>
 * Each {@code Future} eventually completes successfully or fails. If a
 * success, the {@code Future} in question will contain a {@link Result#value()
 * value}. If a failure, it will contain a {@link Result#fault() fault}.
 * <p>
 * In order for the {@link Result} of any given {@code Future} to be received,
 * a {@link Consumer} function <i>must</i> be provided to its {@link
 * #onResult(Consumer)} method. Whenever the result of a {@code Future} is not
 * or no longer desired, its {@link #cancel(boolean)} method should always be
 * called.
 * <p>
 * To make it more convenient to handle different kinds of results, the
 * following default methods are provided, categorized after their areas of
 * application:
 * <ol type="A">
 *     <li>
 *         <b>Result Consumption</b>
 *         <p>Receive or ignore the {@link Result} of this {@code Future}.
 *             <ol>
 *                 <li>{@link #onResult(Consumer)}</li>
 *                 <li>{@link #cancel(boolean)}</li>
 *                 <li>{@link #cancel()}</li>
 *                 <li>{@link #onFailure(Consumer)}</li>
 *             </ol>
 *         </p>
 *     </li>
 *     <li>
 *         <b>Side Effects</b>
 *         <p>Pass the {@link Result} of this {@code Future} to a {@link
 *            ThrowingConsumer} <i>and</i> return it in a new {@code Future}.
 *            This is useful for reacting to certain results becoming available
 *            without preventing them from being propagated further.
 *             <ol>
 *                 <li>{@link #ifFailure(Class, ThrowingConsumer)}</li>
 *                 <li>{@link #ifSuccess(ThrowingConsumer)}</li>
 *                 <li>{@link #always(ThrowingConsumer)}</li>
 *             </ol>
 *         </p>
 *     </li>
 *     <li>
 *         <b>Result Transformation</b>
 *         <p>Transform the {@link Result} of this {@code Future} and return a
 *            new {@code Future} containing the potentially transformed value
 *            or fault. The methods can be divided into two categories: (1)
 *            <i>plain mappers</i> and (2) <i>flat mappers</i>. The former
 *            category of methods simply transform the value or fault of this
 *            {@code Future}, while the latter both transform and await the
 *            completion of that transformation.
 *             <ol>
 *                 <li>{@link #map(ThrowingFunction)}</li>
 *                 <li>{@link #mapCatch(Class, ThrowingFunction)}</li>
 *                 <li>{@link #mapFault(Class, ThrowingFunction)}</li>
 *                 <li>{@link #mapResult(ThrowingFunction)}</li>
 *                 <li>{@link #mapThrow(ThrowingFunction)}</li>
 *                 <li>{@link #flatMap(ThrowingFunction)}</li>
 *                 <li>{@link #flatMapCatch(Class, ThrowingFunction)}</li>
 *                 <li>{@link #flatMapFault(Class, ThrowingFunction)}</li>
 *                 <li>{@link #flatMapResult(ThrowingFunction)}</li>
 *                 <li>{@link #flatMapThrow(ThrowingFunction)}</li>
 *             </ol>
 *         </p>
 *     </li>
 *     <li>
 *         <b>Result Substitution</b>
 *         <p>Replace the {@link Result} of this {@code Future} with any value
 *            or fault.
 *             <ol>
 *                 <li>{@link #pass(Object)}</li>
 *                 <li>{@link #fail(Throwable)}</li>
 *             </ol>
 *         </p>
 *     </li>
 *     <li>
 *         <b>Result Distribution</b>
 *         <p>Allow for multiple consumers to receive the {@link Result} of
 *            this {@code Future}.
 *             <ol>
 *                 <li>{@link #toAnnouncement()}</li>
 *             </ol>
 *         </p>
 *     </li>
 *     <li>
 *         <b>Result Suspension</b>
 *         <p>Adjust the time it will take for this {@code Future} to complete.
 *             <ol>
 *                 <li>{@link #delay(Duration)}</li>
 *                 <li>{@link #delayUntil(Instant)}</li>
 *             </ol>
 *         </p>
 *     </li>
 *     <li>
 *         <b>Thread Management</b>
 *         <p>Consume the result of this {@code Future} on a separate thread.
 *             <ol>
 *                 <li>{@link #fork(Consumer)}</li>
 *                 <li>{@link #forkJoin(ThrowingFunction)}</li>
 *             </ol>
 *         </p>
 *     </li>
 *     <li>
 *         <b>Result Awaiting</b>
 *         <p>Block current thread until the {@link Result} of this {@code
 *            Future} becomes available.
 *              <ol>
 *                  <li>{@link #await()}</li>
 *                  <li>{@link #await(Duration)}</li>
 *              </ol>
 *         </p>
 *     </li>
 *     <li>
 *         <b>Future Result Predetermination</b>
 *         <p>Create a new {@code Future} with a predetermined {@link Result}.
 *             <ol>
 *                 <li>{@link #done()}</li>
 *                 <li>{@link #success(Object)}</li>
 *                 <li>{@link #failure(Throwable)}</li>
 *                 <li>{@link #of(Result)}</li>
 *             </ol>
 *         </p>
 *     </li>
 * </ol>
 * Implementations of this interface are likely <i>not</i> going to be thread
 * safe. Unless otherwise advertised, sharing individual {@code Futures}
 * between multiple threads may cause race conditions.
 *
 * @param <V> Type of <i>value</i> that can be retrieved if the operation
 *            represented by this {@code Future} succeeds.
 */
@SuppressWarnings("unused")
public interface Future<V> {
    Logger logger = LoggerFactory.getLogger(Future.class);

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
        if (consumer == null) {
            throw new NullPointerException("consumer");
        }
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
    default Future<V> ifSuccess(final ThrowingConsumer<V> consumer) {
        if (consumer == null) {
            throw new NullPointerException("consumer");
        }
        final var future = new FutureConsumption<V>(this);
        onResult(result0 -> {
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
            future.consume(result1);
        });
        return future;
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
        if (consumer == null) {
            throw new NullPointerException("consumer");
        }
        final var future = new FutureConsumption<V>(this);
        onResult(result0 -> {
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
            future.consume(result1);
        });
        return future;
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
    default Future<V> always(final ThrowingConsumer<Result<V>> consumer) {
        if (consumer == null) {
            throw new NullPointerException("consumer");
        }
        final var future = new FutureConsumption<V>(this);
        onResult(result0 -> {
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
            future.consume(result1);
        });
        return future;
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
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var future = new FutureConsumption<U>(this);
        onResult(result0 -> {
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
            future.consume(result1);
        });
        return future;
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
        final ThrowingFunction<T, ? extends V> mapper
    ) {
        if (class_ == null) {
            throw new NullPointerException("class_");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var future = new FutureConsumption<V>(this);
        onResult(result0 -> {
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
            future.consume(result1);
        });
        return future;
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
        final ThrowingFunction<Throwable, Throwable> mapper
    ) {
        if (class_ == null) {
            throw new NullPointerException("class_");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var future = new FutureConsumption<V>(this);
        onResult(result0 -> {
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
            future.consume(result1);
        });
        return future;
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
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var future = new FutureConsumption<U>(this);
        onResult(result0 -> {
            Result<U> result1;
            try {
                result1 = mapper.apply(result0);
            }
            catch (final Throwable throwable) {
                result1 = Result.failure(throwable);
            }
            future.consume(result1);
        });
        return future;
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
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var future = new FutureConsumption<U>(this);
        onResult(result -> {
            Throwable cause;
            if (result.isSuccess()) {
                try {
                    cause = mapper.apply(result.value());
                }
                catch (final Throwable throwable) {
                    cause = throwable;
                }
            }
            else {
                cause = result.fault();
            }
            future.consume(Result.failure(cause));
        });
        return future;
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
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var future = new FutureConsumption<U>(this);
        onResult(result -> {
            Throwable cause;
            if (result.isSuccess()) {
                try {
                    future.consume(mapper.apply(result.value()));
                    return;
                }
                catch (final Throwable throwable) {
                    cause = throwable;
                }
            }
            else {
                cause = result.fault();
            }
            future.consume(Result.failure(cause));
        });
        return future;
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
        final ThrowingFunction<T, ? extends Future<V>> mapper
    ) {
        if (class_ == null) {
            throw new NullPointerException("class_");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var future = new FutureConsumption<V>(this);
        onResult(result0 -> {
            Result<V> result1;
            done:
            {
                if (result0.isSuccess()) {
                    result1 = result0;
                    break done;
                }
                var fault = result0.fault();
                if (class_.isAssignableFrom(fault.getClass())) {
                    try {
                        future.consume(mapper.apply(class_.cast(fault)));
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
            future.consume(result1);
        });
        return future;
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
        final ThrowingFunction<Throwable, ? extends Future<Throwable>> mapper
    ) {
        if (class_ == null) {
            throw new NullPointerException("class_");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var future = new FutureConsumption<V>(this);
        onResult(result0 -> {
            Result<V> result1;
            if (result0.isSuccess()) {
                result1 = result0;
            }
            else {
                Throwable cause1 = result0.fault();
                if (class_.isAssignableFrom(cause1.getClass())) {
                    try {
                        final var future1 = mapper.apply(cause1);
                        future1.onResult(result -> future.consume(Result.failure(result.isSuccess()
                            ? result.value()
                            : result.fault())));
                        return;
                    }
                    catch (final Throwable throwable) {
                        cause1 = throwable;
                    }
                }
                result1 = Result.failure(cause1);
            }
            future.consume(result1);
        });
        return future;
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
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var future = new FutureConsumption<U>(this);
        onResult(result -> {
            try {
                future.consume(mapper.apply(result));
            }
            catch (final Throwable throwable) {
                future.consume(Result.failure(throwable));
            }
        });
        return future;
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
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var future = new FutureConsumption<V>(this);
        onResult(result0 -> {
            Result<V> result1;
            if (result0.isFailure()) {
                result1 = result0;
            }
            else {
                try {
                    final var future1 = mapper.apply(result0.value());
                    future1.onResult(result -> future.consume(Result.failure(result.isSuccess()
                        ? result.value()
                        : result.fault())));
                    return;
                }
                catch (final Throwable throwable) {
                    result1 = Result.failure(throwable);
                }
            }
            future.consume(result1);
        });
        return future;
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
        final var future = new FutureConsumption<U>(this);
        onResult(result -> future.consume(result.isSuccess()
            ? Result.success(value)
            : Result.failure(result.fault())));
        return future;
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
        if (throwable == null) {
            throw new NullPointerException("throwable");
        }
        final var future = new FutureConsumption<U>(this);
        onResult(result -> {
            if (result.isFailure()) {
                throwable.addSuppressed(result.fault());
            }
            future.consume(Result.failure(throwable));
        });
        return future;
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
        if (duration == null) {
            throw new NullPointerException("duration");
        }
        final var future = new FutureConsumptionWithExtraCancelTarget<V>(this);
        onResult(result -> future.extraCancelTarget(Schedulers.fixed()
            .schedule(duration, () -> future.consume(result))));
        return future;
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
        if (baseline == null) {
            throw new NullPointerException("baseline");
        }
        final var future = new FutureConsumptionWithExtraCancelTarget<V>(this);
        onResult(result -> {
            final var duration = Duration.between(baseline, Instant.now());
            if (duration.isNegative() || duration.isZero()) {
                future.consume(result);
            }
            else {
                future.extraCancelTarget(Schedulers.fixed()
                    .schedule(duration, () -> future.consume(result)));
            }
        });
        return future;
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
        if (consumer == null) {
            throw new NullPointerException("consumer");
        }
        final var future = new FutureConsumption<>(this);
        onResult(result0 -> {
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
                                logger.error("Unexpected fork consumer exception caught", throwable);
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
            future.consume(result1);
        });
        return future;
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
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var future = new FutureConsumptionWithExtraCancelTarget<U>(this);
        onResult(result0 -> {
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

                final var future1 = Schedulers.dynamic().submit(() -> {
                    Result<U> result1;
                    try {
                        result1 = Result.success(mapper.apply(result0.value()));
                    }
                    catch (final Throwable throwable) {
                        result1 = Result.failure(throwable);
                    }
                    final var result2 = result1;
                    try {
                        eventLoop.execute(() -> future.consume(result2));
                    }
                    catch (final Throwable throwable) {
                        if (throwable instanceof RejectedExecutionException && eventLoop.isShuttingDown()) {
                            return;
                        }
                        logger.error("Failed to join fork", throwable);
                    }
                });
                future.extraCancelTarget(future1);
                return;
            }
            future.consume(Result.failure(fault1));
        });
        return future;
    }

    /**
     * Blocks the current thread until this {@code Future} completes, and then
     * either returns its value or throws its fault.
     * <p>
     * Beware that awaiting a single {@code Future} more than once, either
     * using this method or {@link #await(Duration)}, is <b>not safe</b> and is
     * likely to result in deadlocks or stalls.
     * <p>
     * Using this method injudiciously may result in severe performance issues.
     *
     * @return Value of this {@code Future}, if successful.
     * @throws IllegalStateException If this method is called from a thread
     *                               that might fail to complete this Future if
     *                               blocked.
     * @throws InterruptedException  If the thread awaiting the completion of
     *                               this {@code Future} would be interrupted.
     */
    default V await() throws InterruptedException {
        throwIfThisThreadBelongsToFixedScheduler();
        final var result = new AtomicReference<Result<V>>();
        onResult(result0 -> {
            result.set(result0);
            synchronized (Future.this) {
                Future.this.notify();
            }
        });
        synchronized (this) {
            while (true) {
                final var result0 = result.get();
                if (result0 != null) {
                    return result0.valueOrThrow();
                }
                wait();
            }
        }
    }

    /**
     * Blocks the current thread either until this {@code Future} completes or
     * the given {@code timeout} expires. If this {@code Future} completes
     * before the timeout expires, its value or fault is returned or thrown,
     * respectively.
     * <p>
     * Beware that awaiting a single {@code Future} more than once, either
     * using this method or {@link #await()}, is <b>not safe</b> and is likely
     * to result in deadlocks or stalls.
     * <p>
     * Using this method injudiciously may result in severe performance issues.
     *
     * @param timeout Duration after which the waiting thread is no longer
     *                blocked and a {@link TimeoutException}, unless the result
     *                of this {@code Future} has become available.
     * @return Value of this {@code Future}, if successful.
     * @throws IllegalStateException If this method is called from a thread
     *                               that might fail to complete this Future if
     *                               blocked.
     * @throws InterruptedException  If the thread awaiting the completion of
     *                               this {@code Future} would be interrupted.
     */
    default V await(final Duration timeout) throws InterruptedException, TimeoutException {
        throwIfThisThreadBelongsToFixedScheduler();
        final var result = new AtomicReference<Result<V>>();
        onResult(result0 -> {
            result.set(result0);
            synchronized (Future.this) {
                Future.this.notify();
            }
        });
        var timeoutNanos = timeout.toNanos();
        var last = System.nanoTime();
        synchronized (this) {
            while (true) {
                final var result0 = result.get();
                if (result0 != null) {
                    return result0.valueOrThrow();
                }
                wait(timeoutNanos / 1000000, (int) (timeoutNanos % 1000000000));
                final var curr = System.nanoTime();
                timeoutNanos -= (curr - last);
                if (timeoutNanos <= 0) {
                    break;
                }
                last = curr;
            }
        }
        throw new TimeoutException("Result of " + this + " did not become available in " + timeout);
    }

    private void throwIfThisThreadBelongsToFixedScheduler() {
        if (Thread.currentThread() instanceof NettyThread) {
            throw new IllegalStateException("Netty threads may not be " +
                "blocked by waiting; if you believe to have a use case that " +
                "justifies blocking such a thread, please open a discussion " +
                "with the Kalix developers");
        }
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