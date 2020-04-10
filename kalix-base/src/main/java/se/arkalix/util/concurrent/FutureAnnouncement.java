package se.arkalix.util.concurrent;

import se.arkalix.internal.util.concurrent.FutureCompletion;
import se.arkalix.util.Result;
import se.arkalix.util.annotation.ThreadSafe;

import java.util.HashSet;
import java.util.Set;

/**
 * Announces when the result of some {@link Future} becomes available to a set
 * of subscribers.
 * <p>
 * All subscribers are guaranteed to be notified about the completion of the
 * {@code Future} in question on the same thread.
 *
 * @param <V> Type of value that will be contained in the announced {@link
 *            Result} if the {@code Future} in question completes
 *            successfully.
 */
@SuppressWarnings("unused")
public class FutureAnnouncement<V> {
    private final Future<V> future;
    private final Set<FutureCompletion<V>> subscribers = new HashSet<>();

    private Result<V> result = null;

    FutureAnnouncement(final Future<V> future) {
        this.future = future;
        future.onResult(result -> {
            synchronized (this) {
                this.result = result;
                for (final var subscriber : subscribers) {
                    subscriber.complete(result);
                }
                subscribers.clear();
            }
        });
    }

    /**
     * Signals that the result of this {@code FutureAnnouncement} no longer is
     * of interest and that it is not be announced to any subscribers.
     * <p>
     * No guarantees whatsoever are given about any further implications of
     * this call. The completer of the {@link Future} this announcement
     * contains may receive a notification about the cancellation, and might
     * react to receiving it.
     *
     * @param mayInterruptIfRunning Whether or not the thread executing the
     *                              task associated with the {@code Future}
     *                              wrapped by this announcement, if any,
     *                              should be interrupted. If not, in-progress
     *                              tasks are allowed to complete. This
     *                              parameter may be ignored.
     */
    void cancel(final boolean mayInterruptIfRunning) {
        future.cancel(mayInterruptIfRunning);
    }

    /**
     * Subscribes to the completion of the {@link Future} contained in this
     * {@code FutureAnnouncement}.
     * <p>
     * All subscribers are guaranteed to be notified about the completion of
     * the {@code Future} in question on the same thread, which means that even
     * if another subscriber may have mutated the value before your subscriber
     * was notified, there is no risk for race conditions. If the result
     * already has been announced when this method is called, the returned
     * {@code Future} is completed immediately with the result.
     *
     * @return Future completed with the announced value, when it becomes
     * available.
     */
    @ThreadSafe
    public Future<V> subscribe() {
        final FutureCompletion<V> completion;
        synchronized (this) {
            if (result != null) {
                return Future.of(result);
            }
            completion = new FutureCompletion<V>();
            subscribers.add(completion);
        }
        completion.setCancelFunction(ignored -> {
            synchronized (this) {
                subscribers.remove(completion);
            }
        });
        return completion;
    }
}
