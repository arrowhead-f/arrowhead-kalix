package se.arkalix.concurrent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

public interface SchedulerFuture<T> extends Future<T>, ScheduledFuture<T> {
    /**
     * Returns the remaining delay associated with this object.
     *
     * @return The remaining delay; zero or negative values indicate that the
     * delay has already elapsed.
     * @see #getDelay(TimeUnit)
     */
    default Duration getDelay() {
        return Duration.of(getDelay(TimeUnit.NANOSECONDS), ChronoUnit.NANOS);
    }

    /**
     * Waits for no more than just after the given {@code timeout} for the
     * computation represented by this {@link Future} to complete, and then
     * retrieves its value, if available.
     *
     * @param timeout The maximum time to wait for the value to become
     *                available.
     * @return The computed value.
     * @throws CancellationException If the computation was cancelled.
     * @throws ExecutionException    If the computation threw an exception.
     * @throws InterruptedException  If the current thread was interrupted while
     *                               waiting.
     * @throws TimeoutException      If the wait timed out.
     * @see #get(long, TimeUnit)
     */
    default T get(final Duration timeout) throws InterruptedException, ExecutionException, TimeoutException {
        return get(timeout.toNanos(), TimeUnit.NANOSECONDS);
    }
}
