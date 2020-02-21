package eu.arrowhead.kalix.util.concurrent;

import eu.arrowhead.kalix.ArrowheadLogger;
import eu.arrowhead.kalix.ArrowheadScheduler;
import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.function.ThrowingFunction;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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
 * or any method that uses one of them internally, as it may be the case that
 * the operation represented by the future will not start running until either
 * of these methods are invoked. Failing to call any of these methods may lead
 * to memory never being reclaimed.
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
     * <p>
     * Calling this method must be thread safe.
     *
     * @param consumer Function invoked when this {@code Future} completes.
     * @throws NullPointerException If the consumer function is {@code null}.
     */
    void onResult(final Consumer<Result<V>> consumer);

    /**
     * Signals that the result of this {@code Future} no longer is of interest.
     * <p>
     * If this {@code Future} has already been cancelled or completed, calling
     * this method should do nothing. Calling this method on a {@code Future}
     * that has not yet completed should prevent {@link #onResult(Consumer)}
     * from ever being called.
     * <p>
     * Calling this method must be thread safe.
     */
    void cancel();

    default void onValueOrLog(final Consumer<? super V> consumer) {
        onResult(result -> {
            if (result.isSuccess()) {
                consumer.accept(result.value());
            }
            else {
                ArrowheadLogger.log(result.error());
            }
        });
    }

    default void onError(final Consumer<Throwable> consumer) {
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
     * Any exception thrown by {@code mapper} should lead to the returned
     * future being failed with the same exception.
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
                source.onResult(r0 -> {
                    Result<U> r1;
                    success:
                    {
                        Throwable err;
                        if (r0.isSuccess()) {
                            try {
                                r1 = Result.success(mapper.apply(r0.value()));
                                break success;
                            }
                            catch (final Throwable error) {
                                err = error;
                            }
                        }
                        else {
                            err = r0.error();
                        }
                        r1 = Result.failure(err);
                    }
                    consumer.accept(r1);
                });
            }

            @Override
            public void cancel() {
                source.cancel();
            }
        };
    }

    /**
     * Returns new {@code Future} that is completed when the {@code Future}
     * returned by {@code mapper} completes, which, in turn, is not executed
     * until this {@code Future} completes.
     * <p>
     * The difference between this method and {@link #map(ThrowingFunction)} is that the
     * {@code mapper} provided here is expected to return a {@code Future}
     * rather than a plain value. The returned {@code Future} completes after
     * this {@code Future} and the {@code Future} returned by {@code mapper}
     * have completed in sequence.
     * <p>
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
                source.onResult(r0 -> {
                    Throwable err;
                    if (r0.isSuccess()) {
                        try {
                            final var f1 = mapper.apply(r0.value());
                            f1.onResult(consumer);
                            cancelTarget.set(f1);
                            return;
                        }
                        catch (final Throwable error) {
                            err = error;
                        }
                    }
                    else {
                        err = r0.error();
                    }
                    consumer.accept(Result.failure(err));
                });
            }

            @Override
            public void cancel() {
                final var target = cancelTarget.getAndSet(null);
                if (target != null) {
                    target.cancel();
                }
            }
        };
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

}