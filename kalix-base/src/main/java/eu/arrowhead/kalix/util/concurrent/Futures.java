package eu.arrowhead.kalix.util.concurrent;

import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.function.ThrowingBiFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Various utilities for working will collections of {@link Future Futures}.
 */
public final class Futures {
    private Futures() {}

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
    public static <T, U> Future<U> reduce(
        final Future<T>[] array,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends U> accumulator)
    {
        return reduce(Arrays.asList(array), identity, accumulator);
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
     * @param stream      Stream of futures.
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
    public static <T, U> Future<U> reduce(
        final Stream<Future<T>> stream,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends U> accumulator)
    {
        return reduce(stream.iterator(), identity, accumulator);
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
    public static <T, U> Future<U> reduce(
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
    public static <T, U> Future<U> reduce(
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
    public static <T, U> Future<U> flatReduce(
        final Future<T>[] array,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends Future<U>> accumulator)
    {
        return flatReduce(Arrays.asList(array), identity, accumulator);
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
     * @param stream      Stream of futures.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @return Future, completed with accumulated value only if all futures of
     * {@code stream} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    public static <T, U> Future<U> flatReduce(
        final Stream<Future<T>> stream,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends Future<U>> accumulator)
    {
        return flatReduce(stream.iterator(), identity, accumulator);
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
    public static <T, U> Future<U> flatReduce(
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
    public static <T, U> Future<U> flatReduce(
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
     * @param array       Array of elements.
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
    public static <T, U> Future<U> flatReducePlain(
        final T[] array,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends Future<U>> accumulator)
    {
        return flatReducePlain(Arrays.asList(array), identity, accumulator);
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
     * @param stream      Stream of elements.
     * @param identity    Initial input to the accumulator function.
     * @param accumulator Function used to combine the last identity value with
     *                    the successful result of a {@code Future} into
     *                    another {@code Future}, which in turn completes with
     *                    the next identity value.
     * @param <T>         Type of value provided by futures in array if they
     *                    complete successfully.
     * @param <U>         Type of identity value and accumulated value.
     * @return Future, completed with accumulated value only if all futures of
     * {@code stream} completes successfully. Otherwise it is failed with the
     * first encountered error.
     */
    public static <T, U> Future<U> flatReducePlain(
        final Stream<T> stream,
        final U identity,
        final ThrowingBiFunction<? super U, ? super T, ? extends Future<U>> accumulator)
    {
        return flatReducePlain(stream.iterator(), identity, accumulator);
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
     * @param iterable    Iterable of elements.
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
    public static <T, U> Future<U> flatReducePlain(
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
     * @param iterator    Iterator of elements.
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
    public static <T, U> Future<U> flatReducePlain(
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

    /**
     * Executes every future in {@code array} in sequence, collecting every
     * result into a list.
     *
     * @param array Array of futures.
     * @param <V>   Type of value futures completes with if successful.
     * @return Future completed with list of results from all provided futures,
     * with the results being in the same order as the futures in the given
     * {@code array}.
     */
    public static <V> Future<List<Result<? extends V>>> serialize(final Future<V>[] array) {
        return serialize(Arrays.asList(array));
    }

    /**
     * Executes every future in {@code stream} in sequence, collecting every
     * result into a list.
     *
     * @param stream Stream of futures.
     * @param <V>    Type of value futures completes with if successful.
     * @return Future completed with list of results from all provided futures,
     * with the results being in the same order as the futures in the given
     * {@code stream}.
     */
    public static <V> Future<List<Result<? extends V>>> serialize(final Stream<? extends Future<? extends V>> stream) {
        return serialize(stream.iterator());
    }

    /**
     * Executes every future in {@code iterable} in sequence, collecting every
     * result into a list.
     *
     * @param iterable Iterable of futures.
     * @param <V>      Type of value futures completes with if successful.
     * @return Future completed with list of results from all provided futures,
     * with the results being in the same order as the futures in the given
     * {@code iterable}.
     */
    public static <V> Future<List<Result<? extends V>>> serialize(final Iterable<? extends Future<? extends V>> iterable) {
        return serialize(iterable.iterator());
    }

    /**
     * Executes every future in {@code iterator} in sequence, collecting every
     * result into a list.
     *
     * @param iterator Iterator of futures.
     * @param <V>      Type of value futures completes with if successful.
     * @return Future completed with list of results from all provided futures,
     * with the results being in the same order as the futures in the given
     * {@code iterator}.
     */
    public static <V> Future<List<Result<? extends V>>> serialize(final Iterator<? extends Future<? extends V>> iterator) {
        return flatReducePlain(iterator, new ArrayList<>(), (list, future) ->
            future.mapResult(result -> {
                list.add(result);
                return Result.success(list);
            }));
    }
}
