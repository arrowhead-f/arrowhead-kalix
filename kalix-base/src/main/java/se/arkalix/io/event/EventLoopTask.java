package se.arkalix.io.event;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public record EventLoopTask(Callable<?> task, long baselineInMillis) {
    public EventLoopTask {
        Objects.requireNonNull(task);
    }

    public static EventLoopTask of(final Callable<?> task) {
        return new EventLoopTask(task, 0);
    }

    public static EventLoopTask of(final Runnable task, final Object result) {
        return new EventLoopTask(() -> {
            task.run();
            return result;
        }, 0);
    }

    public static EventLoopTask of(final Runnable task) {
        return new EventLoopTask(() -> {
            task.run();
            return null;
        }, 0);
    }

    public static EventLoopTask of(final Runnable task, final long delay, final TimeUnit unit) {
        return new EventLoopTask(() -> {
            task.run();
            return null;
        }, unit.toMillis(delay));
    }

    public static EventLoopTask of(final Callable<?> task, final long delay, final TimeUnit unit) {
        return new EventLoopTask(task, unit.toMillis(delay));
    }

    //SchedulerFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit)

    //SchedulerFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit)
}
