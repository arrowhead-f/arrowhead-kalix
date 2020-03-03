package eu.arrowhead.kalix.util.concurrent;

import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.function.ThrowingBiFunction;
import eu.arrowhead.kalix.util.function.ThrowingFunction;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.*;
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
     * that has not yet completed does not prevent {@link #onResult(Consumer)}
     * from being called, but rather ensures it eventually will be called with
     * an error of type {@link CancellationException}.
     *
     * @param mayInterruptIfRunning Whether or not the thread executing the
     *                              task associated with this {@code Future},
     *                              should be interrupted. If not, in-progress
     *                              tasks are allowed to complete. This
     *                              parameter is not guaranteed to be honored.
     */
    void cancel(final boolean mayInterruptIfRunning);

    /**
     * Signals that the result of this {@code Future} no longer is of interest
     * and that evaluation of the {@code Future} should be gracefully
     * terminated.
     * <p>
     * If this {@code Future} has already been cancelled or completed, calling
     * this method should do nothing. Calling this method on a {@code Future}
     * that has not yet completed does not prevent {@link #onResult(Consumer)}
     * from being called, but rather ensures it will eventually be called with
     * an error of type {@link CancellationException}.
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
    default <U> Future<U> map(final ThrowingFunction<? super V, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        final var source = this;
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<U>> consumer) {
                source.onResult(result0 -> {
                    Result<U> result1;
                    success:
                    {
                        Throwable err;
                        if (result0.isSuccess()) {
                            try {
                                result1 = Result.success(mapper.apply(result0.value()));
                                break success;
                            }
                            catch (final Throwable error) {
                                err = error;
                            }
                        }
                        else {
                            err = result0.error();
                        }
                        result1 = Result.failure(err);
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
     * Catches any error produced by this {@code Future} and uses
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
     * @param mapper The mapping function to apply to the error of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the error of this {@code Future}.
     * @throws NullPointerException If the mapping function is {@code null}.
     */
    default Future<V> mapError(final ThrowingFunction<Throwable, ? extends V> mapper) {
        Objects.requireNonNull(mapper);
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
                            result1 = Result.success(mapper.apply(result0.error()));
                        }
                        catch (final Throwable error) {
                            result1 = Result.failure(error);
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
        Objects.requireNonNull(mapper);
        final var source = this;
        final var cancelTarget = new AtomicReference<Future<?>>(this);
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<U>> consumer) {
                source.onResult(result0 -> {
                    Throwable err;
                    if (cancelTarget.get() == null) {
                        err = new CancellationException();
                    }
                    else if (result0.isSuccess()) {
                        try {
                            final var future1 = mapper.apply(result0.value());
                            future1.onResult(consumer);
                            cancelTarget.set(future1);
                            return;
                        }
                        catch (final Throwable error) {
                            err = error;
                        }
                    }
                    else {
                        err = result0.error();
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
     * Catches any error produced by this {@code Future} and uses
     * {@code mapper} to transform it into a new value.
     * <p>
     * The difference between this method and
     * {@link #mapError(ThrowingFunction)} is that this method expects its
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
     * @param mapper The mapping function to apply to the error of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the error of this {@code Future}, and then waiting
     * for the {@code Future} returned by the mapper to complete.
     * @throws NullPointerException If the mapping function is {@code null}.
     */
    default Future<V> flatMapError(final ThrowingFunction<Throwable, ? extends Future<V>> mapper) {
        Objects.requireNonNull(mapper);
        final var source = this;
        final var cancelTarget = new AtomicReference<Future<?>>(this);
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<V>> consumer) {
                source.onResult(result0 -> {
                    Result<V> result1;
                    done:
                    {
                        Throwable err;
                        if (cancelTarget.get() == null) {
                            err = new CancellationException();
                        }
                        else {
                            if (result0.isSuccess()) {
                                result1 = result0;
                                break done;
                            }
                            try {
                                final var f1 = mapper.apply(result0.error());
                                f1.onResult(consumer);
                                cancelTarget.set(f1);
                                return;
                            }
                            catch (final Throwable error) {
                                err = error;
                            }
                        }
                        result1 = Result.failure(err);
                    }
                    consumer.accept(result1);
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
     * Creates new {@code Future} that always succeeds with {@code null}.
     *
     * @return New {@code Future}.
     */
    static Future<?> done() {
        return new FutureSuccess<>(null);
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
     * Applies given {@code accumulator} to every element in {@code array} in
     * a way that corresponds with the following code:
     * <pre>
     *     U value = identity;
     *     for (T element : array) {
     *         // Wait for element to become available.
     *         value = accumulator.apply(value, element);
     *     }
     *     return value; // Accumulated value.
     * </pre>
     * In other words, the array elements are processed <i>serially</i>, from
     * the first to the last. The waiting for each element to become available
     * is performed in a non-blocking manner.
     *
     * @param array       Array of futures.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into the
     *                    next identity value.
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @return Future, completed with accumulated value only if all futures in
     * {@code array} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    static <T, U> Future<U> reduce(
        final Future<T>[] array,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends U> accumulator)
    {
        return reduce(Arrays.asList(array), identity, accumulator);
    }

    /**
     * Applies given {@code accumulator} to every element in {@code iterable}
     * in a way that corresponds with the following code:
     * <pre>
     *     U value = identity;
     *     Iterator&lt;T&gt; iterator = iterable.iterator();
     *     while (iterator.hasNext()) {
     *         T element = iterator.next();
     *         // Wait for element to become available.
     *         value = accumulator.apply(value, element);
     *     }
     *     return value; // Accumulated value.
     * </pre>
     * In other words, the elements of the {@code iterable} are processed
     * <i>serially</i>, from the first to the last. The waiting for each
     * element to become available is performed in a non-blocking manner.
     *
     * @param iterable    Iterable of futures.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into the
     *                    next identity value.
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @return Future, completed with accumulated value only if all futures in
     * {@code array} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    static <T, U> Future<U> reduce(
        final Iterable<Future<T>> iterable,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends U> accumulator)
    {
        return reduce(iterable.iterator(), identity, accumulator);
    }

    /**
     * Applies given {@code accumulator} to every element in {@code iterator}
     * in a way that corresponds with the following code:
     * <pre>
     *     U value = identity;
     *     while (iterator.hasNext()) {
     *         T element = iterator.next();
     *         // Wait for element to become available.
     *         value = accumulator.apply(value, element);
     *     }
     *     return value; // Accumulated value.
     * </pre>
     * In other words, the elements of the {@code iterator} are processed
     * <i>serially</i>, from the first to the last. The waiting for each
     * element to become available is performed in a non-blocking manner.
     *
     * @param iterator    Iterator of futures.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into the
     *                    next identity value.
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @return Future, completed with accumulated value only if all futures in
     * {@code array} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    static <T, U> Future<U> reduce(
        final Iterator<Future<T>> iterator,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends U> accumulator)
    {
        if (!iterator.hasNext()) {
            return Future.success(identity);
        }
        try {
            return iterator.next()
                .flatMap(element -> reduce(iterator, accumulator.apply(identity, element), accumulator));
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    /**
     * Applies given {@code accumulator} to every element in {@code array} in
     * a way that corresponds with the following code:
     * <pre>
     *     U value0 = identity;
     *     for (T element : array) {
     *         // Wait for element to become available.
     *         U value1 = accumulator.apply(value0, element);
     *         // Wait for value1 to become available.
     *         value0 = value1;
     *     }
     *     return value0; // Accumulated value.
     * </pre>
     * In other words, the array elements are processed <i>serially</i>, from
     * the first to the last. The waiting for each element to become available
     * is performed in a non-blocking manner.
     *
     * @param array       Array of futures.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @return Future, completed with accumulated value only if all futures in
     * {@code array} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    static <T, U> Future<U> flatReduce(
        final Future<T>[] array,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends Future<U>> accumulator)
    {
        return flatReduce(Arrays.asList(array), identity, accumulator);
    }

    /**
     * Applies given {@code accumulator} to every element of {@code iterable}
     * in a way that corresponds with the following code:
     * <pre>
     *     U value0 = identity;
     *     Iterator&lt;T&gt; iterator = iterable.iterator();
     *     while (iterator.hasNext()) {
     *         T element = iterator.next();
     *         // Wait for element to become available.
     *         U value1 = accumulator.apply(value0, element);
     *         // Wait for value1 to become available.
     *         value0 = value1;
     *     }
     *     return value0; // Accumulated value.
     * </pre>
     * In other words, the elements of the iterable are processed
     * <i>serially</i>, from the first to the last. The waiting for each
     * element to become available is performed in a non-blocking manner.
     *
     * @param iterable    Iterable of futures.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @return Future, completed with accumulated value only if all futures of
     * {@code iterable} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    static <T, U> Future<U> flatReduce(
        final Iterable<Future<T>> iterable,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends Future<U>> accumulator)
    {
        return flatReduce(iterable.iterator(), identity, accumulator);
    }

    /**
     * Applies given {@code accumulator} to every element in {@code iterator}
     * in a way that corresponds with the following code:
     * <pre>
     *     U value0 = identity;
     *     while (iterator.hasNext()) {
     *         T element = iterator.next();
     *         // Wait for element to become available.
     *         U value1 = accumulator.apply(value0, element);
     *         // Wait for value1 to become available.
     *         value0 = value1;
     *     }
     *     return value0; // Accumulated value.
     * </pre>
     * In other words, the elements of the iterator are processed
     * <i>serially</i>, from the first to the last. The waiting for each
     * element to become available is performed in a non-blocking manner.
     *
     * @param iterator    Iterator of futures.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @return Future, completed with accumulated value only if all futures in
     * {@code iterator} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    static <T, U> Future<U> flatReduce(
        final Iterator<Future<T>> iterator,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends Future<U>> accumulator)
    {
        if (!iterator.hasNext()) {
            return Future.success(identity);
        }
        try {
            return iterator.next()
                .flatMap(element -> accumulator.apply(identity, element))
                .flatMap(element -> flatReduce(iterator, element, accumulator));
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    /**
     * Applies given {@code accumulator} to every element in {@code array} in
     * a way that corresponds with the following code:
     * <pre>
     *     U value0 = identity;
     *     for (T element : array) {
     *         U value1 = accumulator.apply(value0, element);
     *         // Wait for value1 to become available.
     *         value0 = value1;
     *     }
     *     return value0; // Accumulated value.
     * </pre>
     * In other words, the array elements are processed <i>serially</i>, from
     * the first to the last. The waiting for each element to become available
     * is performed in a non-blocking manner.
     *
     * @param array       Array of plain elements.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @return Future, completed with accumulated value only if all futures in
     * {@code array} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    static <T, U> Future<U> flatReducePlain(
        final T[] array,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends Future<U>> accumulator)
    {
        return flatReducePlain(Arrays.asList(array), identity, accumulator);
    }

    /**
     * Applies given {@code accumulator} to every element of {@code iterable}
     * in a way that corresponds with the following code:
     * <pre>
     *     U value0 = identity;
     *     Iterator&lt;T&gt; iterator = iterable.iterator();
     *     while (iterator.hasNext()) {
     *         T element = iterator.next();
     *         U value1 = accumulator.apply(value0, element);
     *         // Wait for value1 to become available.
     *         value0 = value1;
     *     }
     *     return value0; // Accumulated value.
     * </pre>
     * In other words, the elements of the iterable are processed
     * <i>serially</i>, from the first to the last. The waiting for each
     * element to become available is performed in a non-blocking manner.
     *
     * @param iterable    Iterable of plain elements.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @return Future, completed with accumulated value only if all futures of
     * {@code iterable} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    static <T, U> Future<U> flatReducePlain(
        final Iterable<T> iterable,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends Future<U>> accumulator)
    {
        return flatReducePlain(iterable.iterator(), identity, accumulator);
    }

    /**
     * Applies given {@code accumulator} to every element in {@code iterator}
     * in a way that corresponds with the following code:
     * <pre>
     *     U value0 = identity;
     *     while (iterator.hasNext()) {
     *         T element = iterator.next();
     *         U value1 = accumulator.apply(value0, element);
     *         // Wait for value1 to become available.
     *         value0 = value1;
     *     }
     *     return value0; // Accumulated value.
     * </pre>
     * In other words, the elements of the iterator are processed
     * <i>serially</i>, from the first to the last. The waiting for each
     * element to become available is performed in a non-blocking manner.
     *
     * @param iterator    Iterator of plain elements.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @return Future, completed with accumulated value only if all futures in
     * {@code iterator} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    static <T, U> Future<U> flatReducePlain(
        final Iterator<T> iterator,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends Future<U>> accumulator)
    {
        if (!iterator.hasNext()) {
            return Future.success(identity);
        }
        try {
            return accumulator.apply(identity, iterator.next())
                .flatMap(element -> flatReducePlain(iterator, element, accumulator));
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }
}