package se.arkalix.util.concurrent;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Various utilities for working will collections of {@link Future Futures}.
 */
public final class Futures {
    private Futures() {}

    /**
     * Blocks the current thread until this {@code Future} completes, and then
     * either returns its value or throws its fault.
     * <p>
     * Beware that awaiting a single {@code Future} more than once, either using
     * this method or {@link #await(Future, Duration)}, is <b>not safe</b> and is likely
     * to result in deadlocks or stalls.
     * <p>
     * Using this method injudiciously may result in severe performance issues.
     *
     * @return Value of this {@code Future}, if successful.
     * @throws IllegalStateException If this method is called from a thread that
     *                               might fail to complete this Future if
     *                               blocked.
     * @throws InterruptedException  If the thread awaiting the completion of
     *                               this {@code Future} would be interrupted.
     */
    public static <V> V await(final Future<V> future) throws InterruptedException {
        if (future == null) {
            throw new NullPointerException("future");
        }

        final var result = new AtomicReference<Result<V>>();
        future.await(result0 -> {
            result.set(result0);
            synchronized (future) {
                future.notify();
            }
        });
        synchronized (future) {
            while (true) {
                final var result0 = result.get();
                if (result0 != null) {
                    return result0.valueOrThrow();
                }
                future.wait();
            }
        }
    }

    /**
     * Blocks the current thread either until this {@code Future} completes or
     * the given {@code timeout} expires. If this {@code Future} completes
     * before the timeout expires, its value or fault is returned or thrown,
     * respectively.
     * <p>
     * Beware that awaiting a single {@code Future} more than once, either using
     * this method or {@link #await(Future)}, is <b>not safe</b> and is likely to
     * result in deadlocks or stalls.
     * <p>
     * Using this method injudiciously may result in severe performance issues.
     *
     * @param timeout Duration after which the waiting thread is no longer
     *                blocked and a {@link TimeoutException}, unless the result
     *                of this {@code Future} has become available.
     * @return Value of this {@code Future}, if successful.
     * @throws IllegalStateException If this method is called from a thread that
     *                               might fail to complete this Future if
     *                               blocked.
     * @throws InterruptedException  If the thread awaiting the completion of
     *                               this {@code Future} would be interrupted.
     * @throws TimeoutException      If this does not complete its operation
     *                               before the given {@code timeout}.
     */
    public static <V> V await(final Future<V> future, final Duration timeout) throws InterruptedException, TimeoutException {
        if (future == null) {
            throw new NullPointerException("future");
        }
        if (timeout == null) {
            throw new NullPointerException("timeout");
        }

        final var result = new AtomicReference<Result<V>>();
        future.await(result0 -> {
            result.set(result0);
            synchronized (future) {
                future.notify();
            }
        });
        var timeoutNanos = timeout.toNanos();
        var last = System.nanoTime();
        synchronized (future) {
            while (true) {
                final var result0 = result.get();
                if (result0 != null) {
                    return result0.valueOrThrow();
                }
                future.wait(timeoutNanos / 1000000, (int) (timeoutNanos % 1000000000));
                final var curr = System.nanoTime();
                timeoutNanos -= (curr - last);
                if (timeoutNanos <= 0) {
                    break;
                }
                last = curr;
            }
        }
        throw new TimeoutException("Result of " + future + " did not become available in " + timeout);
    }

    public static <V> Future<List<Result<V>>> collect(final Future<V>[] array) {
        return collect(List.of(array), array.length);
    }

    public static <V> Future<List<Result<V>>> collect(final Stream<? extends Future<V>> stream) {
        return collect(stream.iterator());
    }

    public static <V> Future<List<Result<V>>> collect(final Stream<? extends Future<V>> stream, final int length) {
        return collect(stream.iterator(), length);
    }

    public static <V> Future<List<Result<V>>> collect(final Iterable<? extends Future<V>> iterable) {
        return collect(iterable.iterator(), iterable instanceof Collection<?> ? ((Collection<?>) iterable).size() : 0);
    }

    public static <V> Future<List<Result<V>>> collect(final Iterable<? extends Future<V>> iterable, final int length) {
        return collect(iterable.iterator(), length);
    }

    public static <V> Future<List<Result<V>>> collect(final Iterator<? extends Future<V>> iterator) {
        return collect(iterator, 0);
    }

    public static <V> Future<List<Result<V>>> collect(final Iterator<? extends Future<V>> iterator, final int length) {
        if (iterator == null) {
            throw new NullPointerException("iterator");
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException();
        }

        final var results = new ArrayList<Result<V>>(length);
        final var promise = new Promise<List<Result<V>>>();

        final var isDone = new AtomicBoolean(false);
        final var numberOfExpectedResults = new AtomicInteger(0);

        for (Future<V> next; (next = iterator.next()) != null; ) {
            results.add(null);

            final var index = numberOfExpectedResults.getAndIncrement();
            if (index < 0) {
                throw new IllegalStateException("Cannot collect more than Integer.MAX_VALUE future results");
            }

            next.await(result -> {
                results.set(index, result);

                if (numberOfExpectedResults.decrementAndGet() == 0) {
                    promise.fulfill(results);
                }
            });
        }
        
        isDone.set(true);

        if (numberOfExpectedResults.decrementAndGet() == 0) {
            promise.fulfill(results);
        }

        return promise.future();
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
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into the
     *                    next identity value.
     * @param array       Array of futures.
     * @return Future, completed with accumulated value only if all futures in
     * {@code array} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    public static <T, U> Future<U> reduce(
        final U identity,
        final BiFunction<? super U, ? super T, ? extends U> accumulator,
        final Future<T>[] array
    ) {
        return reduce(identity, accumulator, Arrays.asList(array));
    }

    /**
     * Applies given {@code accumulator} to every element in {@code stream}
     * in a way that corresponds with the following code:
     * <pre>
     *     U value = identity;
     *     Iterator&lt;T&gt; iterator = stream.iterator();
     *     while (iterator.hasNext()) {
     *         T element = iterator.next();
     *         // Wait for element to become available.
     *         value = accumulator.apply(value, element);
     *     }
     *     return value; // Accumulated value.
     * </pre>
     * In other words, the elements of the {@code stream} are processed
     * <i>serially</i>, from the first to the last. The waiting for each
     * element to become available is performed in a non-blocking manner.
     *
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into the
     *                    next identity value.
     * @param stream      Stream of futures.
     * @return Future, completed with accumulated value only if all futures in
     * {@code array} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    public static <T, U> Future<U> reduce(
        final U identity,
        final BiFunction<? super U, ? super T, ? extends U> accumulator,
        final Stream<Future<T>> stream
    ) {
        return reduce(identity, accumulator, stream.iterator());
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
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into the
     *                    next identity value.
     * @param iterable    Iterable of futures.
     * @return Future, completed with accumulated value only if all futures in
     * {@code array} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    public static <T, U> Future<U> reduce(
        final U identity,
        final BiFunction<? super U, ? super T, ? extends U> accumulator,
        final Iterable<Future<T>> iterable
    ) {
        return reduce(identity, accumulator, iterable.iterator());
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
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into the
     *                    next identity value.
     * @param iterator    Iterator of futures.
     * @return Future, completed with accumulated value only if all futures in
     * {@code array} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    public static <T, U> Future<U> reduce(
        final U identity,
        final BiFunction<? super U, ? super T, ? extends U> accumulator,
        final Iterator<Future<T>> iterator
    ) {
        if (!iterator.hasNext()) {
            return Future.success(identity);
        }
        try {
            return iterator.next()
                .flatMap(element -> reduce(accumulator.apply(identity, element), accumulator, iterator));
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
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param array       Array of futures.
     * @return Future, completed with accumulated value only if all futures in
     * {@code array} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    public static <T, U> Future<U> flatReduce(
        final U identity,
        final BiFunction<? super U, ? super T, ? extends Future<U>> accumulator,
        final Future<T>[] array
    ) {
        return flatReduce(identity, accumulator, Arrays.asList(array));
    }

    /**
     * Applies given {@code accumulator} to every element of {@code stream} in
     * a way that corresponds with the following code:
     * <pre>
     *     U value0 = identity;
     *     Iterator&lt;T&gt; iterator = stream.iterator();
     *     while (iterator.hasNext()) {
     *         T element = iterator.next();
     *         // Wait for element to become available.
     *         U value1 = accumulator.apply(value0, element);
     *         // Wait for value1 to become available.
     *         value0 = value1;
     *     }
     *     return value0; // Accumulated value.
     * </pre>
     * In other words, the elements of the stream are processed
     * <i>serially</i>, from the first to the last. The waiting for each
     * element to become available is performed in a non-blocking manner.
     *
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param stream      Stream of futures.
     * @return Future, completed with accumulated value only if all futures of
     * {@code stream} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    public static <T, U> Future<U> flatReduce(
        final U identity,
        final BiFunction<? super U, ? super T, ? extends Future<U>> accumulator,
        final Stream<Future<T>> stream
    ) {
        return flatReduce(identity, accumulator, stream.iterator());
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
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param iterable    Iterable of futures.
     * @return Future, completed with accumulated value only if all futures of
     * {@code iterable} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    public static <T, U> Future<U> flatReduce(
        final U identity,
        final BiFunction<? super U, ? super T, ? extends Future<U>> accumulator,
        final Iterable<Future<T>> iterable
    ) {
        return flatReduce(identity, accumulator, iterable.iterator());
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
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param iterator    Iterator of futures.
     * @return Future, completed with accumulated value only if all futures in
     * {@code iterator} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    public static <T, U> Future<U> flatReduce(
        final U identity,
        final BiFunction<? super U, ? super T, ? extends Future<U>> accumulator,
        final Iterator<Future<T>> iterator
    ) {
        if (!iterator.hasNext()) {
            return Future.success(identity);
        }
        try {
            return iterator.next()
                .flatMap(element -> accumulator.apply(identity, element))
                .flatMap(element -> flatReduce(element, accumulator, iterator));
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
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param array       Array of elements.
     * @return Future, completed with accumulated value only if all futures in
     * {@code array} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    public static <T, U> Future<U> flatReducePlain(
        final U identity,
        final BiFunction<? super U, ? super T, ? extends Future<U>> accumulator,
        final T[] array
    ) {
        return flatReducePlain(identity, accumulator, Arrays.asList(array));
    }

    /**
     * Applies given {@code accumulator} to every element of {@code stream}
     * in a way that corresponds with the following code:
     * <pre>
     *     U value0 = identity;
     *     Iterator&lt;T&gt; iterator = stream.iterator();
     *     while (iterator.hasNext()) {
     *         T element = iterator.next();
     *         U value1 = accumulator.apply(value0, element);
     *         // Wait for value1 to become available.
     *         value0 = value1;
     *     }
     *     return value0; // Accumulated value.
     * </pre>
     * In other words, the elements of the stream are processed
     * <i>serially</i>, from the first to the last. The waiting for each
     * element to become available is performed in a non-blocking manner.
     *
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param stream      Stream of elements.
     * @return Future, completed with accumulated value only if all futures of
     * {@code stream} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    public static <T, U> Future<U> flatReducePlain(
        final U identity,
        final BiFunction<? super U, ? super T, ? extends Future<U>> accumulator,
        final Stream<T> stream
    ) {
        return flatReducePlain(identity, accumulator, stream.iterator());
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
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param iterable    Iterable of elements.
     * @return Future, completed with accumulated value only if all futures of
     * {@code iterable} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    public static <T, U> Future<U> flatReducePlain(
        final U identity,
        final BiFunction<? super U, ? super T, ? extends Future<U>> accumulator,
        final Iterable<T> iterable
    ) {
        return flatReducePlain(identity, accumulator, iterable.iterator());
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
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param iterator    Iterator of elements.
     * @return Future, completed with accumulated value only if all futures in
     * {@code iterator} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    public static <T, U> Future<U> flatReducePlain(
        final U identity,
        final BiFunction<? super U, ? super T, ? extends Future<U>> accumulator,
        final Iterator<T> iterator
    ) {
        if (!iterator.hasNext()) {
            return Future.success(identity);
        }
        try {
            return accumulator.apply(identity, iterator.next())
                .flatMap(element -> flatReducePlain(element, accumulator, iterator));
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    /**
     * Executes every future in {@code array} in sequence, collecting every
     * successful result into a list. If any future fails, the returned future
     * is failed with the same {@code Throwable}. If more than one future fails
     * each subsequent failing future suppresses the exception of the prior one.
     * <p>
     * All futures in {@code array} are guaranteed to be executed,
     * irrespectively of any of them failing.
     *
     * @param <V>   Type of value futures completes with if successful.
     * @param array Array of futures.
     * @return Future completed with list of results from all provided futures,
     * with the results being in the same order as the futures in the given
     * {@code array}.
     */
    public static <V> Future<List<V>> serialize(final Future<? extends V>[] array) {
        return serialize(Arrays.asList(array));
    }

    /**
     * Executes every future in {@code stream} in sequence, collecting every
     * successful result into a list. If any future fails, the returned future
     * is failed with the same {@code Throwable}. If more than one future fails
     * each subsequent failing future suppresses the exception of prior one.
     * <p>
     * All futures in {@code stream} are guaranteed to be executed,
     * irrespectively of any of them failing.
     *
     * @param <V>    Type of value futures completes with if successful.
     * @param stream Stream of futures.
     * @return Future completed with list of results from all provided futures,
     * with the results being in the same order as the futures in the given
     * {@code stream}.
     */
    public static <V> Future<List<V>> serialize(final Stream<? extends Future<? extends V>> stream) {
        return serialize(stream.iterator());
    }

    /**
     * Executes every future in {@code iterable} in sequence, collecting every
     * successful result into a list. If any future fails, the returned future
     * is failed with the same {@code Throwable}. If more than one future fails
     * each subsequent failing future suppresses the exception of prior one.
     * <p>
     * All futures in {@code iterable} are guaranteed to be executed, even if a
     * future before another such in the given collection would fail.
     *
     * @param <V>      Type of value futures completes with if successful.
     * @param iterable Iterable of futures.
     * @return Future completed with list of results from all provided futures,
     * with the results being in the same order as the futures in the given
     * {@code iterable}.
     */
    public static <V> Future<List<V>> serialize(final Iterable<? extends Future<? extends V>> iterable) {
        return serialize(iterable.iterator());
    }

    /**
     * Executes every future in {@code iterator} in sequence, collecting every
     * successful result into a list. If any future fails, the returned future
     * is failed with the same {@code Throwable}. If more than one future fails
     * each subsequent failing future suppresses the exception of the prior
     * one.
     * <p>
     * All futures in {@code iterator} are guaranteed to be executed,
     * irrespectively of any of them failing.
     *
     * @param <V>      Type of value futures completes with if successful.
     * @param iterator Iterator of futures.
     * @return Future completed with list of results from all provided futures,
     * with the results being in the same order as the futures in the given
     * {@code iterator}. Never fails.
     */
    public static <V> Future<List<V>> serialize(final Iterator<? extends Future<? extends V>> iterator) {
        return serializeInner(iterator, new ArrayList<>(), null);
    }

    private static <V> Future<List<V>> serializeInner(
        final Iterator<? extends Future<? extends V>> iterator,
        final List<V> values,
        final Throwable fault)
    {
        if (!iterator.hasNext()) {
            return fault == null
                ? Future.success(values)
                : Future.failure(fault);
        }
        return iterator.next()
            .flatRewrap(result -> {
                final Throwable fault0;
                if (result.isSuccess()) {
                    values.add(result.value());
                    fault0 = fault;
                }
                else {
                    fault0 = result.fault();
                    if (fault != null) {
                        fault0.addSuppressed(fault);
                    }
                }
                return serializeInner(iterator, values, fault0);
            });
    }
}
