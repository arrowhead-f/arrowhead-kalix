package se.arkalix.internal.net.http.client;

import io.netty.handler.logging.LoggingHandler;
import se.arkalix.util.annotation.Internal;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
    protected void initChannel(final SocketChannel ch) {
        if (futureConnection.failIfCancelled()) {
            ch.close();
            return;
        }
        final var pipeline = ch.pipeline()
            .addLast(new LoggingHandler());

        SslHandler sslHandler = null;
        if (sslContext != null) {
            sslHandler = sslContext.newHandler(ch.alloc());
            pipeline.addLast(sslHandler);
        }

        pipeline
            .addLast(new IdleStateHandler(30, 120, 0, TimeUnit.SECONDS))
            .addLast(new HttpClientCodec())
            .addLast(new NettyHttpClientConnectionHandler(futureConnection, sslHandler));
    }
}
