package eu.arrowhead.kalix.util.concurrent;

import eu.arrowhead.kalix.util.Result;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static eu.arrowhead.kalix.internal.util.concurrent.NettyFutures.adapt;

/**
 * A scheduler useful for asynchronous execution of {@link Future}s.
 */
public final class FutureScheduler {
    private static FutureScheduler defaultScheduler = null;
    private static Thread defaultSchedulerShutdownHook;

    private final EventLoopGroup eventLoopGroup;
    private final Set<FutureSchedulerShutdownListener> shutdownListeners = Collections.synchronizedSet(new HashSet<>());

    private FutureScheduler(final ThreadFactory threadFactory, final int nThreads) {
        final var os = System.getProperty("os.name", "").toLowerCase();
        EventLoopGroup eventLoopGroup = null;
        try {
            if (os.contains("linux")) {
                eventLoopGroup = new EpollEventLoopGroup(nThreads, threadFactory);
            }
            else if (os.contains("bsd")) {
                eventLoopGroup = new KQueueEventLoopGroup(nThreads, threadFactory);
            }
        }
        catch (final UnsatisfiedLinkError ignored) {}
        if (eventLoopGroup == null) {
            eventLoopGroup = new NioEventLoopGroup(nThreads, threadFactory);
        }
        this.eventLoopGroup = eventLoopGroup;
    }

    /**
     * Acquires a reference to the lazily initialized default scheduler.
     * <p>
     * If no scheduler exists when this method is called, a new such with twice
     * as many pooled threads as available CPU cores is created and returned.
     * Otherwise the existing instance is returned immediately.
     * <p>
     * The default scheduler is used automatically by Kalix classes that need a
     * scheduler to operate, but are not explicitly assigned any such when
     * instantiated. The default scheduler differs from other schedulers in
     * that it is automatically assigned its own shutdown hook. This means that
     * if the application is told to terminate via {@link System#exit(int)} or
     * an external interrupt, the default scheduler starts an orderly shutdown
     * with a deadline of 5 seconds. This behavior can be adjusted by setting a
     * default scheduler before any Kalix class gets a chance to call this
     * method.
     *
     * @return Default scheduler instance.
     * @see #setDefault(FutureScheduler)
     * @see #setDefaultWithShutdownHook(FutureScheduler, Duration)
     */
    public synchronized static FutureScheduler getDefault() {
        if (defaultScheduler == null) {
            setDefaultWithShutdownHook(create(), Duration.ofSeconds(5));
        }
        return defaultScheduler;
    }

    /**
     * Sets default scheduler.
     * <p>
     * The default scheduler is used automatically by Kalix classes that need a
     * scheduler to operate, but are not explicitly assigned any such when
     * instantiated.
     * <p>
     * This particular method sets the default scheduler without assigning it a
     * shutdown hook. This means that the caller becomes responsible for
     * ensuring that the scheduler is shut down when no longer in use. Use
     * {@link #setDefaultWithShutdownHook(FutureScheduler, Duration)} if
     * wanting a shutdown hook to be set.
     *
     * @param scheduler Scheduler to use as default scheduler.
     * @throws IllegalStateException If a default scheduler has already been
     *                               set.
     * @see #getDefault()
     */
    public synchronized static void setDefault(final FutureScheduler scheduler) {
        if (defaultScheduler != null) {
            throw new IllegalStateException("A default scheduler is already set");
        }
        defaultScheduler = scheduler;
    }

    /**
     * Sets default scheduler and assigns it a shutdown hook.
     * <p>
     * The default scheduler is used automatically by Kalix classes that need a
     * scheduler to operate, but are not explicitly assigned any such when
     * instantiated.
     * <p>
     * This particular method sets the default scheduler and assigns it a
     * shutdown hook. When triggered, the default scheduler's
     * {@link #shutdown(Duration)} method is invoked with the provided
     * {@code timeout}. Use {@link #setDefault(FutureScheduler)} if not
     * wanting a shutdown hook to be set.
     *
     * @param scheduler Scheduler to use as default scheduler.
     * @throws IllegalStateException If a default scheduler has already been
     *                               set.
     * @see #getDefault()
     */
    public synchronized static void setDefaultWithShutdownHook(
        final FutureScheduler scheduler,
        final Duration timeout)
    {
        if (defaultScheduler != null) {
            throw new IllegalStateException("A default scheduler is already set");
        }
        defaultScheduler = scheduler;
        defaultSchedulerShutdownHook = new Thread(() -> defaultScheduler.shutdown(timeout));
        Runtime.getRuntime().addShutdownHook(defaultSchedulerShutdownHook);
    }

    /**
     * @return New {@code Scheduler} with a thread pool containing twice as
     * many threads as available system CPU cores.
     */
    public static FutureScheduler create() {
        return withThreadFactoryAndNThreads(null, 0);
    }

    /**
     * @param nThreads Number of threads to create for thread pool.
     * @return New {@code Scheduler} with a thread pool containing
     * {@code nThreads} threads.
     */
    public static FutureScheduler withNThreads(final int nThreads) {
        return withThreadFactoryAndNThreads(null, nThreads);
    }

    /**
     * @param threadFactory Factory to use for creating new threads.
     * @return New {@code Scheduler} using given {@code threadFactory} with a
     * thread pool containing twice as many threads as available system CPU
     * cores.
     */
    public static FutureScheduler withThreadFactory(final ThreadFactory threadFactory) {
        return withThreadFactoryAndNThreads(threadFactory, 0);
    }

    /**
     * @param threadFactory Factory to use for creating new threads.
     * @param nThreads      Number of threads to create for thread pool.
     * @return New {@code Scheduler} using given {@code threadFactory} with a
     * thread pool containing {@code nThreads} threads.
     */
    public static FutureScheduler withThreadFactoryAndNThreads(final ThreadFactory threadFactory, final int nThreads) {
        return new FutureScheduler(threadFactory, nThreads);
    }

    /**
     * Adds listener to be notified when this scheduler shuts down.
     * <p>
     * The event is guaranteed to only ever occur once during the life of every
     * {@code FutureScheduler}, and occurs right when shutdown begins. This
     * means that the shutdown listener is able to schedule tasks for a little
     * while before the thread pool is forcibly emptied.
     *
     * @param listener Listener to be notified.
     * @return {@code true} only if {@code listener} was not already registered
     * for receiving shutdown events.
     */
    public boolean addShutdownListener(final FutureSchedulerShutdownListener listener) {
        return shutdownListeners.add(listener);
    }

    /**
     * Removes scheduler shutdown listener.
     *
     * @param listener Listener to no longer be notified when this scheduler
     *                 shuts down.
     * @return {@code true} only if {@code listener} was registered, and now no
     * longer is registered, for receiving shutdown events.
     */
    public boolean removeShutdownListener(final FutureSchedulerShutdownListener listener) {
        return shutdownListeners.remove(listener);
    }

    /**
     * Schedules given {@code command} for execution as soon as reasonably
     * possible.
     *
     * @param command Command to execute.
     */
    public void execute(final Runnable command) {
        eventLoopGroup.execute(command);
    }

    /**
     * Schedules given {@code command} for execution as soon as reasonably
     * possible.
     *
     * @param task Runnable to execute.
     * @return Future completed with {@code null} result when task execution
     * finishes.
     */
    public Future<?> submit(final Runnable task) {
        return adapt(eventLoopGroup.submit(task));
    }

    /**
     * Schedules given {@code callable} for execution as soon as reasonably
     * possible.
     *
     * @param task Callable to execute.
     * @param <V>  Type of value provided by callable if executed
     *             successfully.
     * @return Future completed with result of {@code callable} execution.
     */
    public <V> Future<V> submit(final Callable<V> task) {
        return adapt(eventLoopGroup.submit(task));
    }

    /**
     * Schedules given {@code callable} for execution as soon as reasonably
     * possible.
     *
     * @param task   Task to execute.
     * @param result Result to complete returned {@code Future} with, if
     *               execution is successful.
     * @param <V>    Type of result.
     * @return Future completed when task execution finishes.
     */
    public <V> Future<V> submit(final Runnable task, V result) {
        return adapt(eventLoopGroup.submit(task, result));
    }

    /**
     * Schedules given {@code command} for execution after given {@code delay}.
     *
     * @param command Command to execute.
     * @return Future completed with {@code null} result when task execution
     * finishes.
     */
    public Future<?> scheduleAfter(final Runnable command, final Duration delay) {
        final var millis = delay.toMillis();
        return adapt(eventLoopGroup.schedule(command, millis, TimeUnit.MILLISECONDS));
    }

    /**
     * Schedules given {@code callable} for execution after given {@code delay}.
     *
     * @param callable Callable to execute.
     * @param <V>      Type of value provided by callable if executed
     *                 successfully.
     * @return Future completed with result of {@code callable} execution.
     */
    public <V> Future<V> scheduleAfter(final Callable<V> callable, final Duration delay) {
        final var millis = delay.toMillis();
        return adapt(eventLoopGroup.schedule(callable, millis, TimeUnit.MILLISECONDS));
    }

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
    public Future<?> scheduleAtFixedRate(final Runnable command, final Duration delay, final Duration rate) {
        final var delayMillis = delay.toMillis();
        final var rateMillis = rate.toMillis();
        return adapt(eventLoopGroup.scheduleAtFixedRate(command, delayMillis, rateMillis, TimeUnit.MILLISECONDS));
    }

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
    public Future<?> scheduleWithFixedDelay(final Runnable command, final Duration delay, final Duration rate) {
        final var delayMillis = delay.toMillis();
        final var rateMillis = rate.toMillis();
        return adapt(eventLoopGroup.scheduleWithFixedDelay(command, delayMillis, rateMillis, TimeUnit.MILLISECONDS));
    }

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
     * @return {@code Future} completed after shutdown completion. If there are
     * shutdown listeners and any of them throws an exception when notified
     * about shutdown, the returned {@code Future} is failed, if not already,
     * and any listener exceptions are added to the fault's list of suppressed
     * exceptions.
     */
    public synchronized Future<?> shutdown(final Duration timeout) {
        final var listenerThrowables = new ArrayList<Throwable>(0);
        for (final var listener : shutdownListeners) {
            try {
                listener.onShutdown(this, timeout);
            }
            catch (final Throwable throwable) {
                listenerThrowables.add(throwable);
            }
        }
        synchronized (FutureScheduler.class) {
            if (this == defaultScheduler && defaultSchedulerShutdownHook != null) {
                if (Thread.currentThread() != defaultSchedulerShutdownHook) {
                    Runtime.getRuntime().removeShutdownHook(defaultSchedulerShutdownHook);
                }
            }
        }
        final var millis = timeout.toMillis();
        return adapt(eventLoopGroup.shutdownGracefully(millis / 5, millis, TimeUnit.MILLISECONDS))
            .mapResult(result -> {
                if (listenerThrowables.size() > 0) {
                    if (result.isSuccess()) {
                        final var fault = new Exception("Shutdown successful, but listener exceptions were caught");
                        for (final var throwable : listenerThrowables) {
                            fault.addSuppressed(throwable);
                        }
                        return Result.failure(fault);
                    }
                    else {
                        final var fault = result.fault();
                        for (final var throwable : listenerThrowables) {
                            fault.addSuppressed(throwable);
                        }
                    }
                }
                return result;
            });
    }

    /**
     * @return This scheduler as an {@link ScheduledExecutorService}.
     */
    public ScheduledExecutorService asScheduledExecutorService() {
        return eventLoopGroup;
    }
}
