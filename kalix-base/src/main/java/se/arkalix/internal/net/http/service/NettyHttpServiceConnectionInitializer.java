package se.arkalix.internal.net.http.service;

import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
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

        SslHandler sslHandler = null;
        if (sslContext != null) {
            sslHandler = sslContext.newHandler(ch.alloc());
            pipeline.addLast(sslHandler);
        }
        pipeline
            .addLast(new IdleStateHandler(30, 90, 0, TimeUnit.SECONDS))

            .addLast(new HttpRequestDecoder())
            .addLast(new HttpContentDecompressor())

            .addLast(new HttpRequestEncoder())
            .addLast(new HttpContentCompressor())

            .addLast(new NettyHttpServiceConnectionHandler(serviceLookup, sslHandler));
    }
}
