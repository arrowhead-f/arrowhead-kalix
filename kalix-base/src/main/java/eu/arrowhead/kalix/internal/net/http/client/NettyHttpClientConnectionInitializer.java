package eu.arrowhead.kalix.internal.net.http.client;

import eu.arrowhead.kalix.util.annotation.Internal;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLEngine;
import java.util.Objects;

@Internal
public class NettyHttpClientConnectionInitializer extends ChannelInitializer<SocketChannel> {
    private final HttpResponseReceiver responseReceiver;
    private final SslContext sslContext;

    public NettyHttpClientConnectionInitializer(final HttpResponseReceiver receiver, final SslContext context) {
        responseReceiver = Objects.requireNonNull(receiver, "Expected receiver");
        sslContext = context;
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
            .addLast(new HttpClientCodec()) // TODO: Make message size restrictions configurable.
            .addLast(new HttpContentDecompressor()) // TODO: Make decompression configurable.
            .addLast(new HttpContentCompressor())
            .addLast(new NettyHttpClientConnectionHandler(responseReceiver, sslEngine));
    }
}
