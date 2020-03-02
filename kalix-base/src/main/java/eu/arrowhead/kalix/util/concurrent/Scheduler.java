package eu.arrowhead.kalix.util.concurrent;

import eu.arrowhead.kalix.internal.util.concurrent.NettyScheduler;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;

/**
 * A utility for managing asynchronous task execution.
 */
public interface Scheduler {
    /**
     * @return Default {@code Scheduler} with a thread pool containing twice as
     * many threads as available system CPU cores.
     * <p>
     * Note that the same scheduler will always be returned by this static
     * method, no matter how many times it is called.
     * <p>
     * If ever called, it becomes the responsibility of the caller to ensure
     * that the scheduler is shut down exactly once when no longer in use.
     */
    static Scheduler getDefault() {
        return NettyScheduler.getDefault();
    }

    /**
     * @return New {@code Scheduler} with a thread pool containing twice as
     * many threads as available system CPU cores.
     */
    static Scheduler create() {
        return withThreadFactoryAndNThreads(null, 0);
    }

    /**
     * @param nThreads Number of threads to create for thread pool.
     * @return New {@code Scheduler} with a thread pool containing
     * {@code nThreads} threads.
     */
    static Scheduler withNThreads(final int nThreads) {
        return withThreadFactoryAndNThreads(null, nThreads);
    }

    /**
     * @param threadFactory Factory to use for creating new threads.
     * @return New {@code Scheduler} using given {@code threadFactory} with a
     * thread pool containing twice as many threads as available system CPU
     * cores.
     */
    static Scheduler withThreadFactory(final ThreadFactory threadFactory) {
        return withThreadFactoryAndNThreads(threadFactory, 0);
    }

    /**
     * @param threadFactory Factory to use for creating new threads.
     * @param nThreads      Number of threads to create for thread pool.
     * @return New {@code Scheduler} using given {@code threadFactory} with a
     * thread pool containing {@code nThreads} threads.
     */
    static Scheduler withThreadFactoryAndNThreads(final ThreadFactory threadFactory, final int nThreads) {
        return new NettyScheduler(threadFactory, nThreads);
    }

    /**
     * Schedules given {@code command} for execution as soon as reasonably
     * possible.
     *
     * @param command Command to execute.
     * @return Future completed with {@code null} result when task execution
     * finishes.
     */
    Future<?> schedule(final Runnable command);

    /**
     * Schedules given {@code callable} for execution as soon as reasonably
     * possible.
     *
     * @param callable Callable to execute.
     * @param <V>      Type of value provided by callable if executed
     *                 successfully.
     * @return Future completed with result of {@code callable} execution.
     */
    <V> Future<V> schedule(final Callable<V> callable);

    /**
     * Schedules given {@code command} for execution after given {@code delay}.
     *
     * @param command Command to execute.
     * @return Future completed with {@code null} result when task execution
     * finishes.
     */
    Future<?> scheduleAfter(final Runnable command, final Duration delay);

    /**
     * Schedules given {@code callable} for execution after given {@code delay}.
     *
     * @param callable Callable to execute.
     * @param <V>      Type of value provided by callable if executed
     *                 successfully.
     * @return Future completed with result of {@code callable} execution.
     */
    <V> Future<V> scheduleAfter(final Callable<V> callable, final Duration delay);

    /**
     * Schedules given {@code command} for execution after given {@code delay},
     * after which it is executed repeatedly at given {@code rate}.
     * <p>
     * There are three ways of stopping the execution of given {@code command}:
     * <ol>
     *     <li>Calling {@link Future#cancel(boolean)} on the returned
     *         {@code Future}.</li>
     *     <li>Shutting down the {@code Scheduler}.</li>
     *     <li>Throwing an exception from inside the {@code command}, which
     *         is then propagated to the returned {@code Future}.</li>
     * </ol>
     *
     * @param command Command to execute.
     * @return {@code Future} that completes only if (1) the {@code Scheduler}
     * is terminated, or (2) if {@code command} throws an exception.
     */
    Future<?> scheduleAtFixedRate(final Runnable command, final Duration delay, final Duration rate);

    /**
     * Schedules given {@code command} for execution after given {@code delay}
     * added to given {@code rate}, after which it is executed repeatedly at
     * given {@code rate}.
     * <p>
     * There are three ways of stopping the execution of given {@code command}:
     * <ol>
     *     <li>Calling {@link Future#cancel(boolean)} on the returned
     *         {@code Future}.</li>
     *     <li>Shutting down the {@code Scheduler}.</li>
     *     <li>Throwing an exception from inside the {@code command}, which
     *         is then propagated to the returned {@code Future}.</li>
     * </ol>
     *
     * @param command Command to execute.
     * @return {@code Future} that completes only if (1) the {@code Scheduler}
     * is terminated, or (2) if {@code command} throws an exception.
     */
    Future<?> scheduleWithFixedDelay(final Runnable command, final Duration delay, final Duration rate);

    /**
     * Waits for currently executing tasks to complete before releasing the
     * thread pool and completing the returned {@code Future}, which receives a
     * {@code null} value if shutdown was successful.
     * <p>
     * If given {@code timeout} expires, the currently executing tasks will be
     * terminated forcefully.
     *
     * @param timeout Time after which any lingering tasks will be terminated
     *                forcefully.
     * @return {@code Future} completed after shutdown completion.
     */
    Future<?> shutdown(final Duration timeout);
}
