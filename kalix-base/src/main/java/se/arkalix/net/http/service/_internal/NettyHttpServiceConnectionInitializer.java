package se.arkalix.net.http.service._internal;

import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import se.arkalix.ArSystem;
import se.arkalix.util.annotation.Internal;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * {@link ChannelInitializer} useful for managing incoming HTTP connections.
 */
@Internal
public class NettyHttpServiceConnectionInitializer extends ChannelInitializer<SocketChannel> {
    private final ArSystem system;
    private final HttpServiceLookup serviceLookup;
    private final SslContext sslContext;

    public NettyHttpServiceConnectionInitializer(
        final ArSystem system,
        final HttpServiceLookup serviceLookup,
        final SslContext sslContext)
    {
        this.system = Objects.requireNonNull(system, "system");
        this.serviceLookup = Objects.requireNonNull(serviceLookup, "serviceLookup");
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(final SocketChannel ch) {
        final var pipeline = ch.pipeline();

        SslHandler sslHandler = null;
        if (sslContext != null) {
            sslHandler = sslContext.newHandler(ch.alloc());
            pipeline.addLast(sslHandler);
        }

        pipeline
            .addLast(new LoggingHandler())
            .addLast(new IdleStateHandler(30, 90, 0, TimeUnit.SECONDS))

            .addLast(new HttpServerCodec())

            .addLast(new NettyHttpServiceConnection(system, serviceLookup, sslHandler));
    }
}
