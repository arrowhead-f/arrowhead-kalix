package se.arkalix.util.concurrent;

import se.arkalix.internal.util.concurrent.DynamicScheduler;
import se.arkalix.internal.util.concurrent.NettyScheduler;
import se.arkalix.util.annotation.ThreadSafe;

/**
 * Kalix default schedulers.
 * <p>
 * These schedulers are used by default by all parts of the Kalix library that
 * need scheduling capabilities to operate. They are started up automatically
 * when this class is loaded for the first time, and are shut down
 * automatically when the application is terminated via an OS interrupt signal
 * or by calling {@link System#exit(int)}.
 * <p>
 * Two default schedulers are provided, a <i>fixed</i> and a <i>dynamic</i>.
 * The former is fixed in the sense that it contains a fixed number of pooled
 * threads, chosen to make optimal use of the number of available system CPUs,
 * while the latter is dynamic in the sense that it will contain as many
 * threads as are necessary to prevent pending jobs from having to wait before
 * being executed.
 * <p>
 * Use of the fixed scheduler is preferred for all operations that are
 * <i>non-blocking</i>, which means that they will never suspend the current
 * thread for significant amounts of time (such as for longer than 0.1ms). The
 * reason for the fixed scheduler being preferred is that it makes better use
 * of available CPU resources by switching less between threads, which is a
 * relatively costly operation. As, however, not all operations can be
 * designated as non-blocking, such as old Java I/O calls, JDBC calls,
 * long-running computations, and so on, the dynamic scheduler exists as an
 * alternative. If not used frequently enough, the dynamic scheduler will not
 * contain any cached threads at all, which avoids the costs incurred by
 * context-switching.
 * <p>
 * Furthermore, as a way to minimize synchronization between threads, the fixed
 * size scheduler guarantees that if a job is scheduled by one of its pooled
 * threads, the job will be executed by the thread that scheduled it.
 */
public class Schedulers {
    private static final DynamicScheduler dynamicScheduler = new DynamicScheduler();
    private static final NettyScheduler fixedScheduler = new NettyScheduler();

    static {
        final var runtime = Runtime.getRuntime();

        final Thread hookDynamic = new Thread(dynamicScheduler::shutdown);
        runtime.addShutdownHook(hookDynamic);
        dynamicScheduler.addShutdownListener(scheduler -> {
            try {
                runtime.removeShutdownHook(hookDynamic);
            }
            catch (final IllegalStateException exception) {
                // Ignored.
            }
        });

        final Thread hookFixed = new Thread(fixedScheduler::shutdown);
        runtime.addShutdownHook(hookFixed);
        fixedScheduler.addShutdownListener(scheduler -> {
            try {
                runtime.removeShutdownHook(hookFixed);
            }
            catch (final IllegalStateException exception) {
                // Ignored.
            }
        });
    }

    private Schedulers() {}

    /**
     * @return Reference to scheduler with a fixed-size thread pool.
     * @see Schedulers Class documentation for more details.
     */
    @ThreadSafe
    public static Scheduler fixed() {
        return fixedScheduler;
    }

    /**
     * @return Reference to a scheduler with a dynamically sized thread pool.
     * @see Schedulers Class documentation for more details.
     */
    @ThreadSafe
    public static Scheduler dynamic() {
        return dynamicScheduler;
    }
}
