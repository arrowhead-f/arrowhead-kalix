package se.arkalix.internal.util.concurrent;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.SchedulerShutdownListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Internal
public final class NettyScheduler extends AbstractScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NettyScheduler.class);

    private final EventLoopGroup eventLoopGroup;
    private final Class<? extends SocketChannel> socketChannelClass;
    private final Class<? extends ServerSocketChannel> serverSocketChannelClass;

    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private final Set<SchedulerShutdownListener> shutdownListeners = new HashSet<>();

    public NettyScheduler() {
        final var threadFactory = new NettyThreadFactory();
        final var os = System.getProperty("os.name", "").toLowerCase();

        EventLoopGroup eventLoopGroup0 = null;
        Class<? extends SocketChannel> socketChannelClass0 = null;
        Class<? extends ServerSocketChannel> serverSocketChannelClass0 = null;

        try {
            if (os.contains("linux")) {
                eventLoopGroup0 = new EpollEventLoopGroup(threadFactory);
                socketChannelClass0 = EpollSocketChannel.class;
                serverSocketChannelClass0 = EpollServerSocketChannel.class;
            }
            else if (os.contains("bsd")) {
                eventLoopGroup0 = new KQueueEventLoopGroup(threadFactory);
                socketChannelClass0 = KQueueSocketChannel.class;
                serverSocketChannelClass0 = KQueueServerSocketChannel.class;
            }
        }
        catch (final UnsatisfiedLinkError ignored) {}

        if (eventLoopGroup0 == null) {
            eventLoopGroup0 = new NioEventLoopGroup(threadFactory);
            socketChannelClass0 = NioSocketChannel.class;
            serverSocketChannelClass0 = NioServerSocketChannel.class;
        }

        eventLoopGroup = eventLoopGroup0;
        socketChannelClass = socketChannelClass0;
        serverSocketChannelClass = serverSocketChannelClass0;
    }

    public EventLoopGroup eventLoopGroup() {
        return eventLoopGroup;
    }

    public Class<? extends SocketChannel> socketChannelClass() {
        return socketChannelClass;
    }

    public Class<? extends ServerSocketChannel> serverSocketChannelClass() {
        return serverSocketChannelClass;
    }

    @Override
    protected ScheduledExecutorService executor() {
        return NettyThread.currentThreadEventLoop()
            .orElseGet(eventLoopGroup::next);
    }

    @Override
    public void shutdown() {
        if (isShuttingDown.getAndSet(true)) {
            throw new IllegalStateException("Already shutting down");
        }

        for (final var listener : shutdownListeners) {
            try {
                listener.onShutdown(this);
            }
            catch (final Throwable throwable) {
                logger.error("Unexpected shutdown listener exception caught", throwable);
            }
        }

        eventLoopGroup
            .schedule(this::shutdownNow, 200, TimeUnit.MILLISECONDS)
            .addListener(future -> {
                if (!future.isSuccess()) {
                    logger.error("Failed to delay scheduler shutdown", future.cause());
                    if (!eventLoopGroup.isShuttingDown()) {
                        shutdownNow();
                    }
                }
            });
    }

    private void shutdownNow() {
        eventLoopGroup
            .shutdownGracefully(200, 800, TimeUnit.MILLISECONDS)
            .addListener(future -> {
                if (!future.isSuccess()) {
                    logger.error("Failed to shutdown scheduler", future.cause());
                }
            });
    }

    @Override
    public boolean isShuttingDown() {
        return isShuttingDown.get() || eventLoopGroup.isShuttingDown();
    }

    @Override
    public void addShutdownListener(final SchedulerShutdownListener listener) {
        if (isShuttingDown.get()) {
            throw new IllegalStateException("Already shutting down");
        }
        shutdownListeners.add(listener);
    }

    @Override
    public void removeShutdownListener(final SchedulerShutdownListener listener) {
        if (!isShuttingDown.get()) {
            shutdownListeners.remove(listener);
        }
    }
}
