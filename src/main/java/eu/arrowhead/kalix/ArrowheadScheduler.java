package eu.arrowhead.kalix;

import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.concurrent.Future;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ArrowheadScheduler {
    private static final EventLoopGroup eventLoopGroup;
    private static final Class<? extends SocketChannel> socketChannelClass;
    private static final Class<? extends ServerSocketChannel> serverSocketChannelClass;

    static {
        final var os = System.getProperty("os.name", "")
            .toLowerCase();

        EventLoopGroup group = null;
        Class<? extends SocketChannel> channelClass = null;
        Class<? extends ServerSocketChannel> serverChannelClass = null;
        try {
            if (os.contains("linux")) {
                group = new EpollEventLoopGroup();
                channelClass = EpollSocketChannel.class;
                serverChannelClass = EpollServerSocketChannel.class;
            }
            else if (os.contains("bsd")) {
                group = new KQueueEventLoopGroup();
                channelClass = KQueueSocketChannel.class;
                serverChannelClass = KQueueServerSocketChannel.class;
            }
        }
        catch (final UnsatisfiedLinkError error) {
            ArrowheadLogger.log(error);
        }
        if (group == null) {
            group = new NioEventLoopGroup();
            channelClass = NioSocketChannel.class;
            serverChannelClass = NioServerSocketChannel.class;
        }
        eventLoopGroup = group;
        socketChannelClass = channelClass;
        serverSocketChannelClass = serverChannelClass;
    }

    private ArrowheadScheduler() {}

    public static Future<?> run(final Runnable command) {
        return runAfter(command, Duration.ZERO);
    }

    public static <V> Future<V> run(final Callable<V> callable) {
        return runAfter(callable, Duration.ZERO);
    }

    public static Future<?> runAfter(final Runnable action, final Duration delay) {
        final var millis = delay.toMillis();
        return wrap(eventLoopGroup.schedule(action, millis, TimeUnit.MILLISECONDS));
    }

    public static <V> Future<V> runAfter(final Callable<V> callable, final Duration delay) {
        final var millis = delay.toMillis();
        return wrap(eventLoopGroup.schedule(callable, millis, TimeUnit.MILLISECONDS));
    }

    public static Future<?> repeatAtFixedRate(final Runnable action, final Duration delay, final Duration interval) {
        final var delayMillis = delay.toMillis();
        final var intervalMillis = interval.toMillis();
        return wrap(eventLoopGroup.scheduleAtFixedRate(action, delayMillis, intervalMillis, TimeUnit.MILLISECONDS));
    }

    public static Future<?> repeatWithFixedDelay(final Runnable action, final Duration delay, final Duration interval) {
        final var delayMillis = delay.toMillis();
        final var intervalMillis = interval.toMillis();
        return wrap(eventLoopGroup.scheduleWithFixedDelay(action, delayMillis, intervalMillis, TimeUnit.MILLISECONDS));
    }

    public static void shutdown() {
        shutdownIn(Duration.ofSeconds(3));
    }

    public static void shutdownIn(final Duration duration) {
        final var millis = duration.toMillis();
        eventLoopGroup.shutdownGracefully(millis / 10, millis, TimeUnit.MILLISECONDS);
    }

    private static <V> Future<V> wrap(final ScheduledFuture<V> scheduledFuture) {
        return new ScheduledFutureAdapter<>(scheduledFuture);
    }

    static class ScheduledFutureAdapter<V> implements Future<V> {
        private final ScheduledFuture<V> scheduledFuture;
        private GenericFutureListener<io.netty.util.concurrent.Future<V>> listener = null;

        ScheduledFutureAdapter(final ScheduledFuture<V> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }

        @Override
        public void onResult(final Consumer<Result<V>> consumer) {
            if (listener != null) {
                scheduledFuture.removeListener(listener);
            }
            listener = future -> {
                if (future.isSuccess()) {
                    consumer.accept(Result.success(future.get()));
                }
                else {
                    consumer.accept(Result.failure(future.cause()));
                }
            };
            scheduledFuture.addListener(listener);
        }

        @Override
        public void cancel() {
            if (scheduledFuture.isCancellable()) {
                scheduledFuture.cancel(true);
                if (listener != null) {
                    scheduledFuture.removeListener(listener);
                }
            }
        }
    }
}
