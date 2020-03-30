package se.arkalix.util.concurrent;

/**
 * A listener notified when a {@link Scheduler} shuts down.
 */
@FunctionalInterface
public interface SchedulerShutdownListener {
    /**
     * Called to notify this listener about the given {@code scheduler} being
     * shut down.
     * <p>
     * When this method is called, the scheduler will still accept new jobs for
     * a brief period, and then give any lingering jobs some time to execute
     * before they are forcibly terminated.
     *
     * @param scheduler Scheduler about to be shut down.
     */
    void onShutdown(final Scheduler scheduler);
}
