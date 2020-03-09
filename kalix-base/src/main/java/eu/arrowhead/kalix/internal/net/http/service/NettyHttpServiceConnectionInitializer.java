package eu.arrowhead.kalix.internal.net.http.service;

import eu.arrowhead.kalix.util.annotation.Internal;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLEngine;
import java.util.Objects;

/**
 * {@link ChannelInitializer} useful for managing incoming HTTP connections.
 */
@Internal
public class NettyHttpServiceConnectionInitializer extends ChannelInitializer<SocketChannel> {
    private final HttpServiceLookup serviceLookup;
    private final SslContext sslContext;

    /**
     * @param serviceLookup Function to use for determining what
     *                      {@link eu.arrowhead.kalix.net.http.service.HttpService HttpService}
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
            .addLast(new LoggingHandler())
            .addLast(new HttpRequestDecoder()) // TODO: Make message size restrictions configurable.
            .addLast(new HttpResponseEncoder())
            .addLast(new HttpContentCompressor()) // TODO: Make compression configurable.
            .addLast(new NettyHttpServiceConnectionHandler(serviceLookup, sslEngine));
    }
}
