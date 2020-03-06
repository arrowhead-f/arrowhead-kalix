package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.internal.util.logging.LogLevels;
import eu.arrowhead.kalix.util.logging.LogLevel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLEngine;

public class NettyHttpServiceConnectionInitializer extends ChannelInitializer<SocketChannel> {
    private final HttpServiceLookup serviceLookup;
    private final LogLevel logLevel;
    private final SslContext sslContext;

    public NettyHttpServiceConnectionInitializer(
        final HttpServiceLookup serviceLookup,
        final LogLevel logLevel,
        final SslContext sslContext)
    {
        this.serviceLookup = serviceLookup;
        this.logLevel = logLevel;
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        final var pipeline = ch.pipeline();
        SSLEngine sslEngine = null;
        if (sslContext != null) {
            final var serviceLookup = sslContext.newHandler(ch.alloc());
            sslEngine = serviceLookup.engine();
            pipeline.addLast(serviceLookup);
        }
        pipeline
            .addLast(new LoggingHandler(LogLevels.toNettyLogLevel(logLevel)))
            .addLast(new HttpRequestDecoder()) // TODO: Make message size restrictions configurable.
            .addLast(new HttpResponseEncoder())
            .addLast(new HttpContentCompressor()) // TODO: Make compression configurable.
            .addLast(new NettyHttpServiceRequestHandler(serviceLookup, sslEngine));
    }
}
