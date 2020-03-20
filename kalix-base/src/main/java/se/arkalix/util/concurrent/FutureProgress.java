package se.arkalix.util.concurrent;

/**
 * A {@link Future} that reports on its current progress towards completion.
 *
 * @param <V> Type of value that can be retrieved if the operation succeeds.
 * @see Future
 */
public interface FutureProgress<V> extends Future<V> {
    /**
     * Sets progress listener, replacing any previously set such.
     *
     * @param listener Receiver of progress updates.
     * @return This future, cast to {@link Future}.
     * @throws NullPointerException If {@code listener} is {@code null}.
     */
    Future<V> addProgressListener(Listener listener);

    /**
     * A receiver of progress updates.
     */
    @FunctionalInterface
    interface Listener {
        /**
         * Called to present the current progress towards completion of a
         * {@link FutureProgress}. The only guarantees given regarding the
         * values provided to this method are that {@code current} will be less
         * than or equal to {@code expected}, that both values will be
         * positive, and that {@code current} will grow towards
         * {@code expected} as completion approaches. In other words, the
         * following code will calculate the currently estimated progress
         * towards completion in percent:
         * <pre>
         *     final var progress = ((double) current) / ((double) expected) * 100.0;
         * </pre>
         * Note that {@code expected} may change between invocations of this
         * method.
         *
         * @param current  Current progress.
         * @param expected Expected end goal.
         */
        void onProgress(int current, int expected);
    }

    /**
     * Creates new {@code ProgressFuture} that always succeeds with {@code null}.
     *
     * @return New {@code Future}.
     */
    static FutureProgress<?> done() {
        return new FutureSuccess<>(null);
    }

    /**
     * Creates new {@code ProgressFuture} that always succeeds with {@code value}.
     *
     * @param value Value to wrap in {@code Future}.
     * @param <V>   Type of value.
     * @return New {@code Future}.
     */
    static <V> FutureProgress<V> success(final V value) {
        return new FutureSuccess<>(value);
    }

    /**
     * Creates new {@code ProgressFuture} that always fails with {@code error}.
     *
     * @param error Error to wrap in {@code Future}.
     * @param <V>   Type of value that would have been wrapped if successful.
     * @return New {@code Future}.
     */
    static <V> FutureProgress<V> failure(final Throwable error) {
        return new FutureFailure<>(error);
    }
}
