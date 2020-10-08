package se.arkalix.internal.net.http.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import se.arkalix.internal.util.concurrent.FutureCompletion;
import se.arkalix.net.http.client.HttpClientConnection;
import se.arkalix.util.annotation.Internal;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Internal
public class NettyHttpClientConnectionInitializer extends ChannelInitializer<SocketChannel> {
    private final FutureCompletion<HttpClientConnection> futureConnection;
    private final SslContext sslContext;

    public NettyHttpClientConnectionInitializer(
        final FutureCompletion<HttpClientConnection> futureConnection,
        final SslContext sslContext
    ) {
        this.futureConnection = Objects.requireNonNull(futureConnection, "Expected futureConnection");
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(final SocketChannel channel) {
        if (futureConnection.isCancelled()) {
            channel.close();
            return;
        }
        final var pipeline = channel.pipeline();

        SslHandler sslHandler = null;
        if (sslContext != null) {
            sslHandler = sslContext.newHandler(channel.alloc());
            pipeline.addLast(sslHandler);
        }

        pipeline
            .addLast(new LoggingHandler())
            .addLast(new IdleStateHandler(30, 120, 0, TimeUnit.SECONDS))
            .addLast(new HttpClientCodec())
            .addLast(new NettyHttpClientConnection(futureConnection, channel, sslHandler));
    }
}
