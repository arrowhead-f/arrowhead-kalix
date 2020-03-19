package eu.arrowhead.kalix.internal.net.http.service;

import eu.arrowhead.kalix.util.annotation.Internal;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

import javax.net.ssl.SSLEngine;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * {@link ChannelInitializer} useful for managing incoming HTTP connections.
 */
@Internal
public class NettyHttpServiceConnectionInitializer extends ChannelInitializer<SocketChannel> {
    private final HttpServiceLookup serviceLookup;
    private final SslContext sslContext;

    /**
     * @param serviceLookup Function to use for determining what
     *                      {@link HttpServiceInternal HttpService}
     *                      to forward received requests to.
     * @param sslContext    SSL/TLS context from Netty bootstrap used to
     */
    public NettyHttpServiceConnectionInitializer(final HttpServiceLookup serviceLookup, final SslContext sslContext) {
        this.serviceLookup = Objects.requireNonNull(serviceLookup, "Expected serviceLookup");
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(final SocketChannel ch) {
        final var pipeline = ch.pipeline();
        SSLEngine sslEngine = null;
        if (sslContext != null) {
            final var sslHandler = sslContext.newHandler(ch.alloc());
            sslEngine = sslHandler.engine();
            pipeline.addLast(sslHandler);
        }
        pipeline
            //.addLast(new LoggingHandler(LogLevel.INFO))
            .addLast(new IdleStateHandler(30, 90, 0, TimeUnit.SECONDS)) // TODO: Make configurable.
            .addLast(new HttpServerCodec()) // TODO: Make message size restrictions configurable.
            .addLast(new NettyHttpServiceConnectionHandler(serviceLookup, sslEngine));
    }
}
