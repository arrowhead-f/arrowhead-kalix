package eu.arrowhead.kalix.internal.util.concurrent;

import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.concurrent.Future;
import eu.arrowhead.kalix.util.concurrent.Scheduler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.GenericFutureListener;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A {@code Scheduler} wrapping a Netty {@link EventLoopGroup}.
 * <p>
 * The point of this class is to allow users of this library access to the
 * Netty event loop, without having to expose Netty. The {@code Scheduler}
 * interface is part of the public interface of this library, while this
 * implementation is not.
 */
public class NettyScheduler implements Scheduler {
    private static NettyScheduler defaultScheduler = null;

    private final EventLoopGroup eventLoopGroup;

    /**
     * Creates new {@code Scheduler} from given {@code EventLoopGroup}.
     *
     * @param eventLoopGroup Thread pool to wrap.
     */
    public NettyScheduler(final EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
    }

    /**
     * Creates new {@code Scheduler} that uses {@code threadFactory} for thread
     * creation and fills its thread pool with {@code nThreads} number of
     * threads.
     *
     * @param threadFactory Factory to use for thread creation, or {@code null}
     *                      if the default one is to be used.
     * @param nThreads      The number of desired thread pool threads, or 0 for
     *                      the available number of CPU cores times two.
     */
    public NettyScheduler(final ThreadFactory threadFactory, final int nThreads) {
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
        catch (final UnsatisfiedLinkError ignored) { }
        if (eventLoopGroup == null) {
            eventLoopGroup = new NioEventLoopGroup(nThreads, threadFactory);
        }
        this.eventLoopGroup = eventLoopGroup;
    }

    /**
     * @return Default {@code Scheduler} with a thread pool containing twice as
     * many threads as available system CPU cores.
     * <p>
     * Note that the same scheduler will always be returned by this static
     * method, no matter how many times it is called.
     */
    public static NettyScheduler getDefault() {
        if (defaultScheduler == null) {
            defaultScheduler = new NettyScheduler(null, 0);
        }
        return defaultScheduler;
    }

    @Override
    public Future<?> schedule(final Runnable command) {
        return scheduleAfter(command, Duration.ZERO);
    }

    @Override
    public <V> Future<V> schedule(final Callable<V> callable) {
        return scheduleAfter(callable, Duration.ZERO);
    }

    @Override
    public Future<?> scheduleAfter(final Runnable command, final Duration delay) {
        final var millis = delay.toMillis();
        return wrap(eventLoopGroup.schedule(command, millis, TimeUnit.MILLISECONDS));
    }

    @Override
    public <V> Future<V> scheduleAfter(final Callable<V> callable, final Duration delay) {
        final var millis = delay.toMillis();
        return wrap(eventLoopGroup.schedule(callable, millis, TimeUnit.MILLISECONDS));
    }

    @Override
    public Future<?> scheduleAtFixedRate(final Runnable command, final Duration delay, final Duration rate) {
        final var delayMillis = delay.toMillis();
        final var rateMillis = rate.toMillis();
        return wrap(eventLoopGroup.scheduleAtFixedRate(command, delayMillis, rateMillis, TimeUnit.MILLISECONDS));
    }

    @Override
    public Future<?> scheduleWithFixedDelay(final Runnable command, final Duration delay, final Duration rate) {
        final var delayMillis = delay.toMillis();
        final var rateMillis = rate.toMillis();
        return wrap(eventLoopGroup.scheduleWithFixedDelay(command, delayMillis, rateMillis, TimeUnit.MILLISECONDS));
    }

    @Override
    public Future<?> shutdown(final Duration timeout) {
        final var millis = timeout.toMillis();
        return wrap(eventLoopGroup.shutdownGracefully(millis / 5, millis, TimeUnit.MILLISECONDS));
    }

    /**
     * @return Event loop group wrapped by this {@code Scheduler}.
     */
    public EventLoopGroup eventLoopGroup() {
        return eventLoopGroup;
    }

    private static <V> Future<V> wrap(final io.netty.util.concurrent.Future<V> future) {
        return new FutureAdapter<>(future);
    }

    static class FutureAdapter<V> implements Future<V> {
        private final io.netty.util.concurrent.Future<V> future;
        private GenericFutureListener<io.netty.util.concurrent.Future<V>> listener = null;

        FutureAdapter(final io.netty.util.concurrent.Future<V> future) {
            this.future = future;
        }

        @Override
        public void onResult(final Consumer<Result<V>> consumer) {
            if (listener != null) {
                future.removeListener(listener);
            }
            future.addListener(listener = future -> consumer.accept(future.isSuccess()
                ? Result.success(future.get())
                : Result.failure(future.cause())));
        }

        @Override
        public void cancel(final boolean mayInterruptIfRunning) {
            if (future.isCancellable()) {
                future.cancel(mayInterruptIfRunning);
            }
            if (listener != null) {
                future.removeListener(listener);
            }
        }
    }
}
