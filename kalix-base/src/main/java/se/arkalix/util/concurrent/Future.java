package se.arkalix.util.concurrent;

import se.arkalix.util.annotation.ThreadSafe;
import se.arkalix.util.concurrent._internal.CertainFailure;
import se.arkalix.util.concurrent._internal.CertainSuccess;
import se.arkalix.util.concurrent._internal.ChainedPromise;

import java.util.concurrent.CancellationException;
import java.util.function.*;

/**
 * Represents an operation that will complete at some point in the future with a
 * {@link Result}.
 * <p>
 * Each {@code Future} eventually completes successfully or fails. If a success,
 * the {@code Future} in question will contain a {@link Result#value() value}.
 * If a failure, it will contain a {@link Result#fault() fault}.
 * <p>
 * In order for the {@link Result} of any given {@code Future} to be received, a
 * {@link Consumer} function <i>must</i> be provided to its {@link
 * #await(Consumer)} method. Whenever the result of a {@code Future} is not or
 * no longer desired, its {@link #cancel()} method should be called.
 * <p>
 * To make it more convenient to handle results, the below helper methods are
 * provided, categorized after their areas of application. All except {@link
 * #await(Consumer)} and {@link #cancel()} have default implementations.
 * <ol type="A">
 *     <li>
 *         <b>Result Transformation</b>
 *         <p>Transform the {@link Result} of this {@code Future} and return a
 *            new {@code Future} containing the transformed value or fault.
 *             <ol>
 *                 <li>{@link #map(Function)}
 *                 <li>{@link #flatMap(Function)}
 *                 <li>{@link #rewrap(Function)}
 *                 <li>{@link #zip(Future, BiFunction)}
 *             </ol>
 *     <li>
 *         <b>State Transitioning</b>
 *         <p>Helps turn a failing {@code Future} into a successful such, and
 *            vice versa.
 *             <ol>
 *                 <li>{@link #recover(Function)}
 *                 <li>{@link #recover(Class, Function)}
 *                 <li>{@link #raise(Function)}
 *                 <li>{@link #raiseIf(Predicate, Function)}
 *             </ol>
 *     <li>
 *         <b>Value Substitution</b>
 *         <p>Replace any successful {@link Result} of this {@code Future} with
 *            another value or a fault.
 *             <ol>
 *                 <li>{@link #put(Object)}
 *                 <li>{@link #putIf(Predicate, Object)}
 *             </ol>
 *     <li>
 *         <b>Side Effects</b>
 *         <p>Pass the {@link Result} of this {@code Future} both to a {@link
 *            Consumer} <i>and</i> return it in a new {@code Future}.
 *             <ol>
 *                 <li>{@link #then(Consumer)}
 *             </ol>
 *     <li>
 *         <b>Result Distribution</b>
 *         <p>Allow for multiple consumers to receive the {@link Result} of
 *            this {@code Future}.
 *             <ol>
 *                 <li>{@link #toPublisher()}
 *             </ol>
 *     <li>
 *         <b>Result Predetermination</b>
 *         <p>Create a new {@code Future} with a predetermined {@link Result}.
 *             <ol>
 *                 <li>{@link #done()}
 *                 <li>{@link #success(Object)}
 *                 <li>{@link #failure(Throwable)}
 *                 <li>{@link #of(Result)}
 *             </ol>
 * </ol>
 * The {@code Future} interface is designed primarily to have its {@link Result}
 * both produced <i>and</i> consumed on the same thread. For this reason, <i>no
 * thread safety mechanisms are provided by the default method implementations
 * </i>. Unless otherwise advertised by a specific interface implementation,
 * sharing individual {@code Futures} between multiple threads may cause race
 * conditions.
 *
 * @param <V> Type of <i>value</i> that can be retrieved if the operation
 *            represented by this {@code Future} succeeds.
 */
@SuppressWarnings("unused")
public interface Future<V> {
    /**
     * Sets function to receive result of this {@code Future}, when and if it
     * becomes available.
     * <p>
     * The given consumer function should not throw any exception. Any such will
     * end up with the caller of the {@code consumer}, which is unlikely to be
     * able to handle it in any meaningful way.
     *
     * @param consumer Function invoked when this {@code Future} completes.
     * @throws NullPointerException     If {@code consumer} is {@code null}.
     * @throws FutureAlreadyHasConsumer If a {@code consumer} has already been
     *                                  provided to this {@code Future}. Not
     *                                  guaranteed to be thrown by all
     *                                  implementations.
     */
    void await(final Consumer<Result<V>> consumer);

    /**
     * Signals that the result of this {@code Future} no longer is of interest
     * and that evaluation of the {@code Future} should be terminated.
     * <p>
     * No guarantees whatsoever are given about the implications of this call.
     * It may prevent {@link #await(Consumer)} from being called, or may cause
     * it to be called with a {@link CancellationException}, or something else
     * entirely. However, the receiver of a {@code Future} that is no longer
     * interested in its successful result <i>should</i> call this method to
     * make it clear to the original issuer of the {@code Future}, unless a
     * result has already been received.
     *
     * @return {@code false} only if this {@code Future} could not be cancelled,
     * typically due to it already being completed or cancelled. {@code true}
     * otherwise.
     */
    boolean cancel();

    /**
     * Returns new {@code Future} that is completed after the value of this
     * {@code Future} has become available and could be transformed into a value
     * of type {@code U} by {@code mapper}.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     V value0 = originalFutureOperation();
     *     // Wait for value0 to become available.
     *     U value1 = mapper.apply(value0);
     *     return value1;
     * </pre>
     * Any exception thrown by {@code mapper} leads to the returned {@code
     * Future} being failed with the same exception.
     *
     * @param <U>    The type of the value returned from the mapping function.
     * @param mapper The mapping function to apply to the value of this {@code
     *               Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}, if it completes
     * successfully.
     * @throws NullPointerException If {@code mapper} is {@code null}.
     */
    default <U> Future<U> map(final Function<? super V, U> mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var promise = new ChainedPromise<U>(this);
        await(result0 -> {
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
            promise.complete(result1);
        });
        return promise.future();
    }

    /**
     * Returns new {@code Future} that is completed when the {@code Future}
     * returned by {@code mapper} completes, which, in turn, is not executed
     * until this {@code Future} completes.
     * <p>
     * The difference between this method and {@link #map(Function)} is that
     * the {@code mapper} provided here is expected to return a {@code Future}
     * rather than a plain value. The returned {@code Future} completes after
     * this {@code Future} and the {@code Future} returned by {@code mapper}
     * have completed in sequence.
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
     * Any exception thrown by {@code mapper} should lead to the returned future
     * being failed with the same exception.
     *
     * @param <U>    The type of the value returned from the mapping function.
     * @param mapper The mapping function to apply to the value of this {@code
     *               Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}, and then waiting
     * for the {@code Future} returned by the mapper to complete.
     * @throws NullPointerException If {@code mapper} is {@code null}.
     */
    default <U> Future<U> flatMap(final Function<? super V, ? extends Future<U>> mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var promise = new ChainedPromise<U>(this);
        await(result -> {
            Throwable cause;
            if (result.isSuccess()) {
                try {
                    mapper.apply(result.value())
                        .await(promise::complete);
                    return;
                }
                catch (final Throwable throwable) {
                    cause = throwable;
                }
            }
            else {
                cause = result.fault();
            }
            promise.complete(Result.failure(cause));
        });
        return promise.future();
    }

    /**
     * Takes result of this {@code Future}, when it becomes available, passes it
     * to {@code mapper} and then returns the transformed result wrapped in a
     * new {@code Future}.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     Result&lt;V&gt; result0 = originalFutureOperation();
     *     // Wait for result0 to become available.
     *     Result&lt;U&gt; result1 = mapper.apply(result0);
     *     return result1;
     * </pre>
     * Any exception thrown by {@code mapper} leads to the returned {@code
     * Future} being failed with the same exception.
     *
     * @param <U>    The type of the value returned from the mapping function,
     *               if successful.
     * @param mapper The mapping function to apply to the result of this {@code
     *               Future}, when it becomes available.
     * @return A {@code Future} that should eventually hold the result of
     * applying a mapping function to the result of this {@code Future}.
     * @throws NullPointerException If {@code mapper} is {@code null}.
     */
    default <U> Future<U> rewrap(final Function<Result<V>, Result<U>> mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var promise = new ChainedPromise<U>(this);
        await(result0 -> {
            Result<U> result1;
            try {
                result1 = mapper.apply(result0);
            }
            catch (final Throwable throwable) {
                if (result0.isFailure()) {
                    throwable.addSuppressed(result0.fault());
                }
                result1 = Result.failure(throwable);
            }
            promise.complete(result1);
        });
        return promise.future();
    }

    default <T, U> Future<T> zip(final Future<U> future, final BiFunction<V, U, T> combinator) {
        if (future == null) {
            throw new NullPointerException("future");
        }
        if (combinator == null) {
            throw new NullPointerException("combinator");
        }

        final var promise = new ChainedPromise<T>(this);

        await(result0 -> future.await(result1 -> {
            Throwable fault;
            if (result0.isSuccess()) {
                if (result1.isSuccess()) {
                    try {
                        promise.fulfill(combinator.apply(result0.value(), result1.value()));
                        return;
                    }
                    catch (final Throwable throwable) {
                        fault = throwable;
                    }
                }
                else {
                    fault = result1.fault();
                }
            }
            else {
                fault = result0.fault();
                if (result1.isFailure()) {
                    fault.addSuppressed(result1.fault());
                }
            }
            promise.forfeit(fault);
        }));

        return promise.future();
    }

    /**
     * Catches any fault produced by this {@code Future} and uses {@code mapper}
     * to transform it into a new value.
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
     * Any exception thrown by {@code mapper} leads to the returned {@code
     * Future} being failed with the same exception, which is also provided with
     * the fault of this {@code Future} as a suppressed exception.
     *
     * @param mapper The mapping function to apply to the fault of this {@code
     *               Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the fault of this {@code Future}.
     * @throws NullPointerException If {@code mapper} is {@code null}.
     */
    default Future<V> recover(final Function<Throwable, ? extends V> mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var promise = new ChainedPromise<V>(this);
        await(result0 -> {
            Result<V> result1;
            result:
            if (result0.isSuccess()) {
                result1 = result0;
            }
            else {
                var fault = result0.fault();
                try {
                    result1 = Result.success(mapper.apply(fault));
                    break result;
                }
                catch (final Throwable throwable) {
                    throwable.addSuppressed(fault);
                    fault = throwable;
                }
                result1 = Result.failure(fault);
            }
            promise.complete(result1);
        });
        return promise.future();
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
     * Any exception thrown by {@code mapper} leads to the returned {@code
     * Future} being failed with the same exception.
     *
     * @param <T>    Type of {@link Throwable} caught by this method.
     * @param class_ Class caught exceptions must be assignable to.
     * @param mapper The mapping function to apply to the fault of this {@code
     *               Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the fault of this {@code Future}.
     * @throws NullPointerException If {@code class_} or {@code mapper} is
     *                              {@code null}.
     */
    default <T extends Throwable> Future<V> recover(final Class<T> class_, final Function<T, ? extends V> mapper) {
        if (class_ == null) {
            throw new NullPointerException("class_");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var promise = new ChainedPromise<V>(this);
        await(result0 -> {
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
            promise.complete(result1);
        });
        return promise.future();
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
     * Any exception thrown by {@code mapper} leads to the returned {@code
     * Future} being failed with the same exception.
     *
     * @param mapper The mapping function to apply to the value of this {@code
     *               Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}.
     * @throws NullPointerException If {@code mapper} is {@code null}.
     */
    default Future<V> raise(final Function<? super V, Throwable> mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var promise = new ChainedPromise<V>(this);
        await(result -> {
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
            promise.forfeit(cause);
        });
        return promise.future();
    }

    /**
     * Takes a successful {@link Result} produced by this {@code Future}, if its
     * value satisfies given {@code predicate}, and uses {@code mapper} to
     * transform it into a fault.
     * <p>
     * In other words, this method performs the asynchronous counterpart to the
     * following code:
     * <pre>
     *     V value = originalFutureOperation();
     *     if (predicate.test(value)) {
     *         // Wait for value to become available.
     *         Throwable fault = mapper.apply(value);
     *         throw fault;
     *     }
     *     else {
     *         return value;
     *     }
     * </pre>
     * Any exception thrown by either {@code predicate} or {@code mapper} leads
     * to the returned {@code Future} being failed with the same exception.
     *
     * @param mapper The mapping function to apply to the value of this {@code
     *               Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}.
     * @throws NullPointerException If {@code predicate} or {@code mapper} is
     *                              {@code null}.
     */
    default Future<V> raiseIf(final Predicate<V> predicate, final Function<? super V, Throwable> mapper) {
        if (predicate == null) {
            throw new NullPointerException("predicate");
        }
        if (mapper == null) {
            throw new NullPointerException("mapper");
        }
        final var promise = new ChainedPromise<V>(this);
        await(result -> {
            Result<V> result1;
            if (result.isSuccess() && predicate.test(result.value())) {
                final var value = result.value();
                Throwable fault;
                try {
                    fault = mapper.apply(value);
                }
                catch (final Throwable throwable) {
                    fault = throwable;
                }
                result1 = Result.failure(fault);
            }
            else {
                result1 = result;
            }
            promise.complete(result1);
        });
        return promise.future();
    }

    /**
     * Returns new {@code Future} that succeeds with given {@code value} if this
     * {@code Future} completes successfully.
     * <p>
     * If this {@code Future} fails, on the other hand, the returned {@code
     * Future} is failed with the same fault.
     *
     * @param <U>   Type of {@code value}.
     * @param value Value to include in the returned {@code Future}.
     * @return A {@code Future} that will complete with given {@code throwable}
     * as fault.
     */
    default <U> Future<U> put(final U value) {
        final var promise = new ChainedPromise<U>(this);
        await(result -> promise.complete(result.isSuccess()
            ? Result.success(value)
            : Result.failure(result.fault())));
        return promise.future();
    }

    default <U> Future<U> put(final Supplier<U> supplier) {
        final var promise = new ChainedPromise<U>(this);
        await(result -> promise.complete(result.isSuccess()
            ? Result.success(supplier.get())
            : Result.failure(result.fault())));
        return promise.future();
    }

    default Future<V> putIf(final Predicate<Result<V>> predicate, final V value) {
        final var promise = new ChainedPromise<V>(this);
        await(result -> {
            if (predicate.test(result)) {
                result = Result.success(value);
            }
            promise.complete(result);
        });
        return promise.future();
    }

    default Future<V> putIf(final Predicate<Result<V>> predicate, final Supplier<V> supplier) {
        final var promise = new ChainedPromise<V>(this);
        await(result -> {
            if (predicate.test(result)) {
                result = Result.success(supplier.get());
            }
            promise.complete(result);
        });
        return promise.future();
    }

    /**
     * Returns new {@code Future} that is guaranteed to fail with given {@code
     * throwable}.
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
    default Future<V> raise(final Throwable throwable) {
        if (throwable == null) {
            throw new NullPointerException("throwable");
        }
        final var promise = new ChainedPromise<V>(this);
        await(result -> {
            if (result.isFailure()) {
                throwable.addSuppressed(result.fault());
            }
            promise.forfeit(throwable);
        });
        return promise.future();
    }

    /**
     * Returns new {@code Future} that is failed with the given {@code
     * throwable} if the {@link Result} of this {@code Future} matches the given
     * {@code predicate}. In any other case the returned {@code Future} is
     * completed with the {@link Result} of this {@code Future}.
     *
     * @param predicate Function used to test {@link Result} of this {@code
     *                  Future}.
     * @param throwable Fault to include in returned {@code Future}.
     * @return A {@code Future} that will complete with given {@code throwable}
     * as fault.
     * @throws NullPointerException If {@code predicate} or {@code throwable} is
     *                              {@code null}.
     */
    default Future<V> raiseIf(final Predicate<Result<V>> predicate, final Throwable throwable) {
        if (predicate == null) {
            throw new NullPointerException("predicate");
        }
        if (throwable == null) {
            throw new NullPointerException("throwable");
        }
        final var promise = new ChainedPromise<V>(this);
        await(result -> {
            if (predicate.test(result)) {
                promise.forfeit(throwable);
            }
            else {
                promise.complete(result);
            }
        });
        return promise.future();
    }

    /**
     * Sets function to receive result of this {@code Future}, no matter if it
     * succeeds or not. The result is also passed on to the returned {@code
     * Future}.
     * <p>
     * This method is primarily useful for triggering different kinds of side
     * effects, such as logging or sending messages.
     * <p>
     * Any exception thrown by {@code consumer} leads to the returned {@code
     * Future} being failed with the same exception. If the result of this
     * {@code Future} is a fault, that fault is added as a suppressed exception
     * to the new exception thrown by the {@code consumer} function.
     *
     * @param consumer Function invoked with the result of this {@code Future}.
     * @return New {@code Future} completed with the result of this {@code
     * Future}.
     * @throws NullPointerException If {@code consumer} is {@code null}.
     */
    default Future<V> then(final Consumer<Result<V>> consumer) {
        if (consumer == null) {
            throw new NullPointerException("consumer");
        }
        final var promise = new ChainedPromise<V>(this);
        await(result0 -> {
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
            promise.complete(result1);
        });
        return promise.future();
    }

    /**
     * Converts this {@code Future} into a {@link FuturePublisher}, which allows
     * its result to be received by any number of subscribers.
     * <p>
     * As noted {@link #await(Consumer) here}, a {@code Future} should only ever
     * present its result to a single receiver. This method allows for that
     * scope be widened to zero or more receivers.
     *
     * @return {@link FuturePublisher}, able to  advertise the result of this
     * {@code Future} to any number of subscribers.
     */
    default FuturePublisher<V> toPublisher() {
        return new FuturePublisher<>(this);
    }

    /**
     * Returns {@code Future} that always succeeds with {@code null}.
     *
     * @param <V> Arbitrary type.
     * @return Cached {@code Future}.
     */
    @SuppressWarnings("unchecked")
    @ThreadSafe
    static <V> Future<V> done() {
        return (Future<V>) CertainSuccess.NULL;
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
        return new CertainSuccess<>(value);
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
        return new CertainFailure<>(fault);
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
        return result.isSuccess()
            ? success(result.value())
            : failure(result.fault());
    }
}