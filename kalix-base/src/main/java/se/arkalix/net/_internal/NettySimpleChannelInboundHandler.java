package se.arkalix.net._internal;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import se.arkalix.util.concurrent._internal.NettyThread;
import se.arkalix.util.annotation.Internal;

@Internal
public abstract class NettySimpleChannelInboundHandler<I> extends SimpleChannelInboundHandler<I> {
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        final var thread = Thread.currentThread();
        if (!(thread instanceof NettyThread)) {
            throw new IllegalStateException("Current thread is not a NettyThread");
        }
        ((NettyThread) thread).eventLoop(ctx.channel().eventLoop());
        super.channelActive(ctx);
    }
}
