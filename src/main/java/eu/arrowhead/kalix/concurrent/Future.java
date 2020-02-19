package eu.arrowhead.kalix.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents an operation that will complete at some point in the future.
 * <p>
 * To make it convenient to act on the completion of this {@code Future}, the
 * receiver of it is expected to provide a {@code Consumer} to the
 * {@link #onResult(Consumer)} method, which should be invoked whenever the
 * {@link Result} of the operation becomes available. As an alternative, the
 * {@code Future} receiver may also decide to {@link #cancel()} it, which
 * should mean that any {@code Consumer}, if set, never will be invoked.
 * <p>
 * It is the responsibility of the receiver of a {@code Future} to make sure
 * that either {@link #onResult(Consumer)} and/or {@link #cancel()} is called,
 * as it may be the case that the operation represented by the future will not
 * start running until either of these methods are invoked. Failing to call any
 * of these methods may lead to memory never being reclaimed, which could lead
 * to memory exhaustion at some point in the future.
 *
 * @param <V> Type of value that can be retrieved if the operation succeeds.
 */
public interface Future<V> {
    /**
     * Sets function to receive result of this {@code Future}, when it becomes
     * available.
     * <p>
     * If this method has been called previously on the same object, any
     * previously set consumer should be replaced by this one.
     *
     * @param consumer Function invoked when this {@code Future} completes.
     * @throws NullPointerException If the consumer function is {@code null}.
     */
    void onResult(final Consumer<Result<? extends V>> consumer);

    /**
     * Signals that the result of this {@code Future} no longer is of interest.
     * <p>
     * If this {@code Future} has already completed, calling this method should
     * do nothing. If not, any function provided to {@link #onResult(Consumer)}
     * should never be called.
     */
    void cancel();

    /**
     * Returns new {@code Future} that is completed after the value of this
     * {@code Future} has become available and could be transformed into a
     * value of type {@code U} by {@code mapper}. If this {@code Future} fails,
     * the returned {@code Future} is failed with the same {@code Exception}.
     *
     * @param <U>    The type of the value returned from the mapping function.
     * @param mapper The mapping function to apply to the value of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}, if its value
     * ever becomes available.
     * @throws NullPointerException If the mapping function is {@code null}.
     */
    <U> Future<U> map(final Function<? super V, ? extends U> mapper);

    /**
     * Returns new {@code Future} that is completed after the value of this
     * {@code Future} has become available and could be transformed into a
     * value of type {@code U} by {@code mapper}.
     * <p>
     * The difference between this method and {@link #map(Function)} is that
     * {@code mapper} does not return a plain value, but a {@code Future} that
     * is completed before the {@code Future} returned by this method is
     * completed.
     * <p>
     * If this {@code Future} or the {@code Future} of {@code mapper} fails,
     * the {@code Future} returned by this method is failed with the same
     * {@code Exception}.
     *
     * @param <U>    The type of the value returned from the mapping function.
     * @param mapper The mapping function to apply to the value of this
     *               {@code Future}, if it becomes available.
     * @return A {@code Future} that may eventually hold the result of applying
     * a mapping function to the value of this {@code Future}, and then waiting
     * for the {@code Future} returned by the mapper to complete.
     * @throws NullPointerException If the mapping function is {@code null}.
     */
    <U> Future<U> flatMap(final Function<? super V, ? extends Future<? extends U>> mapper);

    /**
     * A function that may throw any {@link Throwable} while being executed.
     *
     * @param <V> Type of value to be provided to function.
     */
    @FunctionalInterface
    interface Consumer<V> {
        /**
         * Provides consumer with a value.
         *
         * @param v Value to provide.
         * @throws Throwable Any kind of exception.
         */
        void accept(V v) throws Throwable;
    }

    /**
     * A function, converting an input value into an output value. May throw
     * any {@link Throwable} while being executed.
     *
     * @param <V> Type of value to be provided to function.
     * @param <U> Type of value returned from function.
     */
    @FunctionalInterface
    interface Function<V, U> {
        /**
         * Provides consumer with a value and receives its output value.
         *
         * @param v Value to provide.
         * @return Output value.
         * @throws Throwable Any kind of exception.
         */
        U apply(V v) throws Throwable;
    }

    /**
     * The result of a {@link Future}.
     * <p>
     * A {@code Result} may either be a <i>success</i>, in which case a
     * <i>value</i> is available, or a <i>failure</i>, which makes an
     * <i>error</i> available. The {@link #isSuccess()} method is used to
     * determine which of the two situations is the case. The {@link #value()}
     * and {@link #error()} methods are used to collect the value or error.
     *
     * @param <V> Type of value provided by {@code Result} if successful.
     */
    class Result<V> {
        private final boolean isSuccess;
        private final V value;
        private final Throwable error;

        private Result(final boolean isSuccess, final V value, final Throwable error) {
            this.isSuccess = isSuccess;
            this.value = value;
            this.error = error;
        }

        /**
         * Creates new successful {@code Result}.
         *
         * @param value Value.
         * @param <V>   Type of value.
         * @return New {@code Result}.
         */
        public static <V> Result<V> success(final V value) {
            return new Result<>(true, value, null);
        }

        /**
         * Creates new failure {@code Result}.
         *
         * @param error Reason for failure.
         * @param <V>   Type of value that would have been provided by the
         *              created {@code Result}, if it were successful.
         * @return New {@code Result}.
         */
        public static <V> Result<V> failure(final Throwable error) {
            return new Result<>(false, null, error);
        }

        /**
         * @return {@code true} if this {@code Result} contains a value.
         */
        public boolean isSuccess() {
            return isSuccess;
        }

        /**
         * @return Some exception if this {@code Result} is a failure.
         * {@code null} otherwise.
         */
        public Throwable error() {
            return error;
        }

        /**
         * @return Some value if this {@code Result} is a success. {@code null}
         * otherwise.
         */
        public V value() {
            return value;
        }

        /**
         * Either returns {@code Result} value or throws its error, depending
         * on whether it is successful or not.
         * <p>
         * In the case of being a failure, the error is thrown as-is if it is a
         * subclass of {@link RuntimeException}. If not, it is wrapped in a
         * {@code RuntimeException} before being thrown.
         *
         * @return Result value, if the {@code Result} is successful.
         * @throws RuntimeException If the {@code Result} is a failure.
         */
        public V valueOrThrow() {
            if (isSuccess()) {
                return value();
            }
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            }
            throw new RuntimeException(error());
        }
    }
}
