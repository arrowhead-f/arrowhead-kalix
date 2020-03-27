package se.arkalix.internal.util.concurrent;

import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import se.arkalix.util.concurrent.Future;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static se.arkalix.internal.util.concurrent.NettyFutures.adapt;

@Internal
public final class NettyScheduler {
    private static NettyScheduler scheduler = null;

    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private final AtomicInteger referenceCounter = new AtomicInteger(0);
    private final Set<NettySchedulerShutdownListener> shutdownListeners = Collections.synchronizedSet(new HashSet<>());

    private final EventLoopGroup eventLoopGroup;
    private final Thread shutdownHook;

    private NettyScheduler() {
        final var os = System.getProperty("os.name", "").toLowerCase();
        EventLoopGroup eventLoopGroup0 = null;
        try {
            if (os.contains("linux")) {
                eventLoopGroup0 = new EpollEventLoopGroup();
            }
            else if (os.contains("bsd")) {
                eventLoopGroup0 = new KQueueEventLoopGroup();
            }
        }
        catch (final UnsatisfiedLinkError ignored) {}
        if (eventLoopGroup0 == null) {
            eventLoopGroup0 = new NioEventLoopGroup();
        }
        eventLoopGroup = eventLoopGroup0;

        shutdownHook = new Thread(this::shutdown);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public synchronized static NettyScheduler acquire() {
        if (scheduler == null) {
            scheduler = new NettyScheduler();
        }
        scheduler.referenceCounter.incrementAndGet();
        return scheduler;
    }

    public void addShutdownListener(final NettySchedulerShutdownListener listener) {
        shutdownListeners.add(listener);
    }

    public void removeShutdownListener(final NettySchedulerShutdownListener listener) {
        shutdownListeners.remove(listener);
    }

    public void execute(final Runnable command) {
        eventLoopGroup.execute(command);
    }

    public Future<?> submit(final Runnable task) {
        return adapt(eventLoopGroup.submit(task));
    }

    public <V> Future<V> submit(final Callable<V> task) {
        return adapt(eventLoopGroup.submit(task));
    }

    public <V> Future<V> submit(final Runnable task, V result) {
        return adapt(eventLoopGroup.submit(task, result));
    }

    public Future<?> scheduleAfter(final Runnable command, final Duration delay) {
        final var millis = delay.toMillis();
        return adapt(eventLoopGroup.schedule(command, millis, TimeUnit.MILLISECONDS));
    }

    public <V> Future<V> scheduleAfter(final Callable<V> callable, final Duration delay) {
        final var millis = delay.toMillis();
        return adapt(eventLoopGroup.schedule(callable, millis, TimeUnit.MILLISECONDS));
    }

    public Future<?> scheduleAtFixedRate(final Runnable command, final Duration delay, final Duration rate) {
        final var delayMillis = delay.toMillis();
        final var rateMillis = rate.toMillis();
        return adapt(eventLoopGroup.scheduleAtFixedRate(command, delayMillis, rateMillis, TimeUnit.MILLISECONDS));
    }

    public Future<?> scheduleWithFixedDelay(final Runnable command, final Duration delay, final Duration rate) {
        final var delayMillis = delay.toMillis();
        final var rateMillis = rate.toMillis();
        return adapt(eventLoopGroup.scheduleWithFixedDelay(command, delayMillis, rateMillis, TimeUnit.MILLISECONDS));
    }

    public Future<?> release() {
        if (referenceCounter.getAndDecrement() == 1) {
            return shutdown();
        }
        return Future.done();
    }

    public EventLoopGroup eventLoopGroup() {
        return eventLoopGroup;
    }

    public boolean isShuttingDown() {
        return isShuttingDown.get() || eventLoopGroup.isShuttingDown();
    }

    private synchronized Future<?> shutdown() {
        if (isShuttingDown.getAndSet(true)) {
            throw new IllegalStateException("Already shutting down");
        }
        Runtime.getRuntime().removeShutdownHook(shutdownHook);

        final var listenerThrowables = new ArrayList<Throwable>(0);
        for (final var listener : shutdownListeners) {
            try {
                listener.onShutdown(this);
            }
            catch (final Throwable throwable) {
                listenerThrowables.add(throwable);
            }
        }

        final var shutdownConsumer = new AtomicReference<Consumer<Result<Object>>>(null);
        final var shutdownFuture = new AtomicReference<io.netty.util.concurrent.Future<?>>(null);

        final var future0 = eventLoopGroup.schedule(() -> {
            final var future1 = eventLoopGroup.shutdownGracefully(200, 800, TimeUnit.MILLISECONDS);
            if (shutdownFuture.getAndSet(future1) == null) {
                future1.cancel(true);
            }
            else {
                final var consumer = shutdownConsumer.get();
                if (consumer != null) {
                    consumer.accept(Result.success(null));
                }
            }
        }, 200, TimeUnit.MILLISECONDS);

        shutdownFuture.set(future0);

        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<Object>> consumer) {
                shutdownConsumer.set(consumer);
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                final var future = shutdownFuture.getAndSet(null);
                if (future != null) {
                    future.cancel(mayInterruptIfRunning);
                }
            }
        }.mapResult(result -> {
            if (listenerThrowables.size() > 0) {
                if (result.isSuccess()) {
                    final var fault = new Exception("Shutdown successful, " +
                        "but listener exceptions were caught");
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
}
