package se.arkalix.concurrent;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public interface Scheduler extends ScheduledExecutorService {
    /**
     * Blocks until all tasks have completed execution after a shutdown request,
     * or the timeout occurs, or the current thread is interrupted, whichever
     * happens first.
     *
     * @param timeout The maximum time to wait.
     * @return {@code true} if this executor terminated and {@code false} if the
     * timeout elapsed before termination.
     * @throws InterruptedException If interrupted while waiting.
     */
    default boolean awaitTermination(final Duration timeout) throws InterruptedException {
        return awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    <T> SchedulerFuture<T> submit(Callable<T> task);

    @Override
    <T> SchedulerFuture<T> submit(Runnable task, T result);

    @Override
    SchedulerFuture<?> submit(Runnable task);

    default SchedulerFuture<?> schedule(final Runnable command, final Duration delay) {
        return schedule(command, delay.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    SchedulerFuture<?> schedule(Runnable command, long delay, TimeUnit unit);

    default <V> SchedulerFuture<V> schedule(final Callable<V> callable, final Duration delay) {
        return schedule(callable, delay.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    <V> SchedulerFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);

    default SchedulerFuture<?> scheduleAtFixedRate(final Runnable command, final Duration initialDelay, final Duration period) {
        return scheduleAtFixedRate(command, initialDelay.toNanos(), period.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    SchedulerFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

    default SchedulerFuture<?> scheduleWithFixedDelay(final Runnable command, final Duration initialDelay, final Duration delay) {
        return scheduleWithFixedDelay(command, initialDelay.toNanos(), delay.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    SchedulerFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);
}
