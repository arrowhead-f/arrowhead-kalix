package eu.arrowhead.kalix.concurrent;

import eu.arrowhead.kalix.util.function.ThrowingFunction;
import eu.arrowhead.kalix.util.function.ThrowingSupplier;

/**
 * A {@link java.util.concurrent.Future} that notifies a listener when
 * completed, for any reason.
 *
 * @param <V> Type of value that can be retrieved if the operation succeeds.
 */
public interface Future<V> extends java.util.concurrent.Future<V> {
    /**
     * Sets completion listener, replacing any previous such.
     *
     * @param listener Function invoked when the {@link Future} completes.
     * @return This {@link Future}.
     */
    <E extends Throwable>
    Future<V> onDone(final ThrowingSupplier<? extends Future<? extends V>, ? extends E> listener);

    /**
     * Returns new {@code Future} that is completed after the value of this
     * {@code Future} has become available and could be transformed into a
     * value of type {@code U} by {@code mapper}. If this {@code Future} fails
     * or is interrupted, the returned {@code Future} fails or is interrupted
     * with the same {@code Exception}.
     *
     * @param <U>    The type of the value returned from the mapping function.
     * @param mapper The mapping function to apply to the value of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}, if its value
     * ever becomes available.
     * @throws NullPointerException Ff the mapping function is {@code null}.
     */
    <U, E extends Throwable>
    Future<U> map(ThrowingFunction<? super V, ? extends U, ? extends E> mapper);

    /**
     * Returns new {@code Future} that is completed after the value of this
     * {@code Future} has become available and could be transformed into a
     * value of type {@code U} by {@code mapper}.
     * <p>
     * The difference between this method and {@link #map(ThrowingFunction)} is
     * that {@code mapper} returns a {@code Future} that is awaited before the
     * {@code Future} returned by this method is completed.
     * <p>
     * If this {@code Future} or the {@code Future} of {@code mapper} fails or
     * is interrupted, the returned {@code Future} fails or is interrupted
     * with the same {@code Exception}.
     *
     * @param <U>    The type of the value returned from the mapping function.
     * @param mapper The mapping function to apply to the value of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}, and then waiting
     * for the {@code Future} of the mapper to complete.
     * @throws NullPointerException Ff the mapping function is {@code null}.
     */
    <U, E extends Throwable>
    Future<U> flatMap(ThrowingFunction<? super V, ? extends Future<? extends U>, ? extends E> mapper);
}
