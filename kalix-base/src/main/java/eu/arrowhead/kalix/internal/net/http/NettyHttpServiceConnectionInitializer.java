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
    private final HttpServiceRequestHandler handler;
    private final LogLevel logLevel;
    private final SslContext sslContext;

    public NettyHttpServiceConnectionInitializer(
        final HttpServiceRequestHandler handler,
        final LogLevel logLevel,
        final SslContext sslContext)
    {
        this.handler = handler;
        this.logLevel = logLevel;
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        final var pipeline = ch.pipeline();
        SSLEngine sslEngine = null;
        if (sslContext != null) {
            final var handler = sslContext.newHandler(ch.alloc());
            sslEngine = handler.engine();
            pipeline.addLast(handler);
        }
        pipeline
            .addLast(new LoggingHandler(LogLevels.toNettyLogLevel(logLevel)))
            .addLast(new HttpRequestDecoder()) // TODO: Make message size restrictions configurable.
            .addLast(new HttpResponseEncoder())
            .addLast(new HttpContentCompressor()) // TODO: Make compression configurable.
            .addLast(new NettyHttpServiceRequestHandler(handler, sslEngine));
    }
}
