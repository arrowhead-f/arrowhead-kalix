package se.arkalix.util.concurrent;

import se.arkalix.util.annotation.ThreadSafe;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;

/**
 * An asynchronous execution scheduler.
 * <p>
 * A scheduler maintains an arbitrary set of execution resources, most likely
 * some variant of the {@link Thread} class, used to schedule and execute given
 * jobs.
 */
@SuppressWarnings("unused")
public interface Scheduler {
    /**
     * Executes given {@code command} as soon as an execution resource is
     * available.
     *
     * @param command Command to execute.
     * @throws NullPointerException       If {@code command} is {@code null}.
     * @throws RejectedExecutionException If scheduler cannot execute
     *                                    {@code command}.
     */
    @ThreadSafe
    void execute(final Runnable command);

    /**
     * Executes given {@code task} as soon as an execution resource is
     * available.
     *
     * @param task Task to execute.
     * @return {@code Future} completed with {@code null} when the given
     * {@code task} completes. If the task throws an exception while being
     * executed, it is rejected with the same exception. If the scheduler
     * cannot execute the task, the returned {@code Future} is failed with a
     * {@link RejectedExecutionException}.
     * @throws NullPointerException If {@code task} is {@code null}.
     */
    @ThreadSafe
    Future<?> submit(final Runnable task);

    /**
     * Executes given {@code task} as soon as an execution resource i
     * available.
     *
     * @param task Task to execute.
     * @return {@code Future} completed with the {@code task} return value. If
     * the task throws an exception while being executed, it is rejected with
     * the same exception. If the scheduler cannot execute the task, the
     * returned {@code Future} is failed with a
     * {@link RejectedExecutionException}.
     * @throws NullPointerException If {@code task} is {@code null}.
     */
    @ThreadSafe
    <V> Future<V> submit(final Callable<V> task);

    /**
     * Executes given {@code task} as soon as an execution resource is
     * available.
     *
     * @param task Task to execute.
     * @return {@code Future} completed with {@code result} when the given
     * {@code task} completes. If the task throws an exception while being
     * executed, it is rejected with the same exception. If the scheduler
     * cannot execute the task, the returned {@code Future} is failed with a
     * {@link RejectedExecutionException}.
     * @throws NullPointerException If {@code task} is {@code null}.
     */
    @ThreadSafe
    <V> Future<V> submit(final Runnable task, V result);

    /**
     * Executes given {@code command} no sooner than indicated by given
     * {@code delay}.
     *
     * @param delay   Delay after which to execute given {@code command}.
     * @param command Command to execute.
     * @return {@code Future} completed with {@code null} when the given
     * {@code command} completes. If the task throws an exception while being
     * executed, it is rejected with the same exception. If the scheduler
     * cannot execute the command, the returned {@code Future} is failed with
     * a {@link RejectedExecutionException}.
     * @throws NullPointerException If {@code task} is {@code null}.
     */
    @ThreadSafe
    Future<?> schedule(final Duration delay, final Runnable command);

    /**
     * Executes given {@code callable} no sooner than indicated by given
     * {@code delay}.
     *
     * @param delay    Delay after which to execute given {@code callable}.
     * @param callable Callable to execute.
     * @return {@code Future} completed with {@code null} when the given
     * {@code callable} completes. If the callable throws an exception while
     * being executed, it is rejected with the same exception. If the scheduler
     * cannot execute the callable, the returned {@code Future} is failed with
     * a {@link RejectedExecutionException}.
     * @throws NullPointerException If {@code task} is {@code null}.
     */
    @ThreadSafe
    <V> Future<V> schedule(final Duration delay, final Callable<V> callable);

    /**
     * Executes given {@code command} no sooner than indicated by given
     * {@code initialDelay}. The command is then scheduled for repeated
     * execution with the stated {@code rate}. The interval between the start
     * of each execution will remain approximately constant as long as no
     * execution takes more than {@code rate} time to execute.
     * <p>
     * Execution will stop if any of the following three conditions occur:
     * <ol>
     *     <li>The {@link Future#cancel(boolean) cancel} method of the returned
     *     {@code Future} is called.</li>
     *     <li>This scheduler is shut down.</li>
     *     <li>The given {@code command} throws an exception.</li>
     * </ol>
     *
     * @param initialDelay Initial delay after which to execute given
     *                     {@code command} for the first time.
     * @param rate         Interval between repeated {@code command} executions.
     * @param command      Command to execute.
     * @return {@code Future} that never completes successfully. If the given
     * {@code command} throws an exception while being executed, the future is
     * rejected with the same exception. If the scheduler cannot execute the
     * command, the future is failed with a {@link RejectedExecutionException}.
     * @throws NullPointerException If {@code task} is {@code null}.
     */
    @ThreadSafe
    Future<?> scheduleAtFixedRate(final Duration initialDelay, final Duration rate, final Runnable command);

    /**
     * Executes given {@code command} no sooner than indicated by given
     * {@code initialDelay}. When execution finishes, the first and every other
     * time, the command is scheduled for execution again after the given
     * {@code delay}.
     * <p>
     * Execution will stop if any of the following three conditions occur:
     * <ol>
     *     <li>The {@link Future#cancel(boolean) cancel} method of the returned
     *     {@code Future} is called.</li>
     *     <li>This scheduler is shut down.</li>
     *     <li>The given {@code command} throws an exception.</li>
     * </ol>
     *
     * @param initialDelay Initial delay after which to execute given
     *                     {@code command} for the first time.
     * @param delay        Delay between repeated {@code command} executions.
     * @param command      Command to execute.
     * @return {@code Future} that never completes successfully. If the given
     * {@code command} throws an exception while being executed, the future is
     * rejected with the same exception. If the scheduler cannot execute the
     * command, the future is failed with a {@link RejectedExecutionException}.
     * @throws NullPointerException If {@code task} is {@code null}.
     */
    @ThreadSafe
    Future<?> scheduleWithFixedDelay(final Duration initialDelay, final Duration delay, final Runnable command);

    /**
     * @return {@code true} if the scheduler is currently in the process of, or
     * already has, shut down.
     */
    @ThreadSafe
    boolean isShuttingDown();

    /**
     * Registers a listener to be called when the scheduler shuts down.
     *
     * @param listener Listener to be notified of any impending scheduler
     *                 termination.
     */
    @ThreadSafe
    void addShutdownListener(final SchedulerShutdownListener listener);

    /**
     * Unregisters a previously registered listener, making sure it will not be
     * notified when the scheduler shuts down.
     * <p>
     * Calling this method with a listener that was never registered does
     * nothing.
     *
     * @param listener Listener to no longer be notified of any impending
     *                 scheduler termination.
     * @apiNote Thread safe.
     */
    @ThreadSafe
    void removeShutdownListener(final SchedulerShutdownListener listener);
}
