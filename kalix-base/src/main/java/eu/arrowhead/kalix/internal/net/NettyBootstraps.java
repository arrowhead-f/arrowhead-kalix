package eu.arrowhead.kalix.internal.net;

import eu.arrowhead.kalix.internal.util.concurrent.NettyScheduler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyBootstraps {
    private NettyBootstraps() {}

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
