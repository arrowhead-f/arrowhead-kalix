package se.arkalix.internal.net;

import se.arkalix.util.annotation.Internal;
import se.arkalix.internal.util.concurrent.NettyScheduler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
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

@Internal
public class NettyBootstraps {
    private NettyBootstraps() {}

    public static Bootstrap createBootstrapUsing(final NettyScheduler scheduler) {
        final var eventLoopGroup = scheduler.eventLoopGroup();
        final Class<? extends SocketChannel> socketChannelClass;
        if (eventLoopGroup instanceof EpollEventLoopGroup) {
            socketChannelClass = EpollSocketChannel.class;
        }
        else if (eventLoopGroup instanceof KQueueEventLoopGroup) {
            socketChannelClass = KQueueSocketChannel.class;
        }
        else if (eventLoopGroup instanceof NioEventLoopGroup) {
            socketChannelClass = NioSocketChannel.class;
        }
        else {
            throw new IllegalStateException("Unsupported Netty event loop " +
                "group type \"" + eventLoopGroup.getClass() + "\"");
        }
        return new Bootstrap()
            .group(eventLoopGroup)
            .channel(socketChannelClass);
    }

    public static ServerBootstrap createServerBootstrapUsing(final NettyScheduler scheduler) {
        final var eventLoopGroup = scheduler.eventLoopGroup();
        final Class<? extends ServerSocketChannel> socketChannelClass;
        if (eventLoopGroup instanceof EpollEventLoopGroup) {
            socketChannelClass = EpollServerSocketChannel.class;
        }
        else if (eventLoopGroup instanceof KQueueEventLoopGroup) {
            socketChannelClass = KQueueServerSocketChannel.class;
        }
        else if (eventLoopGroup instanceof NioEventLoopGroup) {
            socketChannelClass = NioServerSocketChannel.class;
        }
        else {
            throw new IllegalStateException("Unsupported Netty event loop " +
                "group type \"" + eventLoopGroup.getClass() + "\"");
        }
        return new ServerBootstrap()
            .group(eventLoopGroup)
            .channel(socketChannelClass);
    }

}
