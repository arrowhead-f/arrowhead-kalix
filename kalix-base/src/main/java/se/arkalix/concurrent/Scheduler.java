package se.arkalix.concurrent;

public interface Scheduler {
    /*<T> Future<T> schedule(Task<T> task);

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
    SchedulerFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);*/
}
