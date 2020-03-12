package eu.arrowhead.kalix.internal.net.http.client;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.util.annotation.Internal;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;

import java.util.Objects;

@Internal
public class NettyHttpClientConnectionInitializer extends ChannelInitializer<SocketChannel> {
    private final FutureHttpClientConnection futureConnection;
    private final SslContext sslContext;

    public NettyHttpClientConnectionInitializer(
        final FutureHttpClientConnection futureConnection,
        final SslContext sslContext)
    {
        this.futureConnection = Objects.requireNonNull(futureConnection, "Expected futureConnection");
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        if (futureConnection.failIfCancelled()) {
            ch.close();
            return;
        }
        final var pipeline = ch.pipeline();
        SslHandler sslHandler = null;
        if (sslContext != null) {
            sslHandler = sslContext.newHandler(ch.alloc());
            pipeline.addLast(sslHandler);
        }
        pipeline
            //.addLast(new LoggingHandler(LogLevel.INFO))
            .addLast(new HttpClientCodec()) // TODO: Make message size restrictions configurable.
            //.addLast(new IdleStateHandler(10, 10, 15))
            .addLast(new NettyHttpClientConnectionHandler(futureConnection, sslHandler));
    }
}
