package eu.arrowhead.kalix.internal.net.http.client;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.util.annotation.Internal;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

import java.security.cert.X509Certificate;
import java.util.Objects;

@Internal
public class NettyHttpClientConnectionInitializer extends ChannelInitializer<SocketChannel> {
    private final NettyHttpClient client;
    private final EncodingDescriptor encoding;
    private final SslContext sslContext;

    public NettyHttpClientConnectionInitializer(
        final NettyHttpClient client,
        final EncodingDescriptor encoding,
        final SslContext sslContext)
    {
        this.client = Objects.requireNonNull(client, "Expected client");
        this.encoding = encoding;
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        final var pipeline = ch.pipeline();
        if (sslContext != null) {
            final var sslHandler = sslContext.newHandler(ch.alloc());
            final var chain = sslHandler.engine().getSession().getPeerCertificates();
            final var x509chain = new X509Certificate[chain.length];
            for (var i = 0; i < chain.length; ++i) {
                final var certificate = chain[i];
                if (!(certificate instanceof X509Certificate)) {
                    throw new IllegalStateException("Only x.509 " +
                        "certificates may be used by remote peers, " +
                        "somehow the peer at " + client.remoteSocketAddress() +
                        " was able to use some other type: " + certificate);
                }
                x509chain[i] = (X509Certificate) chain[i];
            }
            client.setCertificateChain(x509chain);
            pipeline.addLast(sslHandler);
        }
        pipeline
            .addLast(new LoggingHandler())
            .addLast(new HttpClientCodec()) // TODO: Make message size restrictions configurable.
            .addLast(new HttpContentDecompressor()) // TODO: Make decompression configurable.
            .addLast(new HttpContentCompressor())
            .addLast(new NettyHttpClientConnectionHandler(encoding, client));
    }
}
