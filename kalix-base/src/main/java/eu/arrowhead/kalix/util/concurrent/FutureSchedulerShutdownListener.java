package eu.arrowhead.kalix.util.concurrent;

import java.time.Duration;

/**
 * A receiver of the {@link FutureScheduler} shutdown event.
 */
@FunctionalInterface
public interface FutureSchedulerShutdownListener {
    /**
     * Called exactly once when the {@link FutureScheduler#shutdown(Duration)}
     * method is called, with the scheduler and duration provided to that
     * method.
     * <p>
     * The shutdown listener is guaranteed to be able to schedule new tasks if
     * done so immediately. Tasks scheduled after this method returns may never
     * be executed or cause exceptions to be thrown. The tasks are guaranteed
     * execution time until the given {@code timeout} expires.
     * <p>
     * Exceptions thrown by this method will not stop the shutdown.
     *
     * @param scheduler Scheduler being shut down.
     * @param timeout   Duration before scheduler thread pool is forcibly
     *                  emptied.
     * @throws Exception Thrown exceptions are collected and added as
     *                   suppressed exceptions to the then failing
     *                   {@link Future} returned by
     *                   {@link FutureScheduler#shutdown(Duration)}.
     */
    void onShutdown(final FutureScheduler scheduler, final Duration timeout) throws Exception;
}
