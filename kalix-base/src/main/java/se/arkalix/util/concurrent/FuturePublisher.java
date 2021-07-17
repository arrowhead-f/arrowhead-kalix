package se.arkalix.util.concurrent;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Publishes the {@link Result} of a {@link Future} to a set of subscribers,
 * when that {@link Result} becomes available.
 * <p>
 * Instances of this class are typically created via {@link
 * Future#toPublisher()}.
 *
 * @param <V> Type of value that will be contained in the published {@link
 *            Result}, if the {@code Future} in question completes
 *            successfully.
 */
public class FuturePublisher<V> {
    private final Future<V> future;
    private final Set<Promise<V>> subscribers = new HashSet<>();

    private Result<V> result = null;

    protected FuturePublisher(final Future<V> future) {
        this.future = Objects.requireNonNull(future, "future");
        future.await(result -> {
            if (this.result != null) {
                return;
            }
            this.result = result;
            for (final var subscriber : subscribers) {
                subscriber.complete(result);
            }
            subscribers.clear();
        });
    }

    /**
     * Signals that the {@link Result} of this {@code FuturePublisher} no longer
     * is of interest and that it is not be published to any subscribers.
     */
    public boolean cancel() {
        if (future.cancel()) {
            subscribers.clear();
            return true;
        }
        return false;
    }

    /**
     * Gets the {@link Result} of this {@code FuturePublisher}, if it already is
     * available.
     *
     * @return {@link Result} of this {@code FuturePublisher}, if available.
     */
    public Optional<Result<V>> resultIfAvailable() {
        return Optional.ofNullable(result);
    }

    /**
     * Subscribes to the completion of the {@link Future} managed by this {@code
     * FuturePublisher}.
     * <p>
     * All subscribers are guaranteed to be notified about the completion of the
     * {@link Future} in question sequentially on the thread producing the
     * {@link Result}. Even if some subscribers mutate the {@link Result}, there
     * is no risk for race conditions.
     * <p>
     * If the {@link Result} already has been published when this method is
     * called, the returned {@link Future} is completed immediately with the
     * {@link Result}.
     *
     * @return Future completed with the announced value, when it becomes
     * available.
     */
    public Future<V> subscribe() {
        if (result != null) {
            return Future.of(result);
        }
        final var promise = new Promise<V>() {
            @Override
            public boolean cancel() {
                if (super.cancel()) {
                    subscribers.remove(this);
                    return true;
                }
                return false;
            }
        };
        subscribers.add(promise);
        return promise;
    }
}
