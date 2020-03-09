package eu.arrowhead.kalix.internal.net.http.service;

import eu.arrowhead.kalix.net.http.service.HttpRequester;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.Optional;

/**
 * Information about the original sender of an incoming HTTP request.
 */
public class NettyHttpRequester implements HttpRequester {
    private final ChannelHandlerContext ctx;
    private final SSLEngine sslEngine;
    private final HttpHeaders headers;

    private X509Certificate[] cachedCertificateChain;
    private InetSocketAddress cachedRemoteSocketAddress;
    private String cachedToken;
    private boolean isTokenCached = false;

    /**
     * Creates new {@link HttpRequester}.
     *
     * @param ctx       Netty channel handler context associated with channel
     *                  used to handle the requester in question.
     * @param headers   Incoming HTTP request headers.
     * @param sslEngine SSL/TLS engine used by channel, if in secure mode.
     */
    public NettyHttpRequester(final ChannelHandlerContext ctx, final HttpHeaders headers, final SSLEngine sslEngine) {
        this.ctx = ctx;
        this.headers = headers;
        this.sslEngine = sslEngine;
    }

    @Override
    public X509Certificate certificate() {
        if (cachedCertificateChain == null) {
            loadCertificateChain();
        }
        return cachedCertificateChain[0];
    }

    @Override
    public X509Certificate[] certificateChain() {
        if (cachedCertificateChain == null) {
            loadCertificateChain();
        }
        return cachedCertificateChain.clone();
    }

    private void loadCertificateChain() {
        if (sslEngine == null) {
            throw new UnsupportedOperationException("Not in secure mode");
        }
        try {
            final var chain = sslEngine.getSession().getPeerCertificates();
            cachedCertificateChain = new X509Certificate[chain.length];
            for (var i = 0; i < chain.length; ++i) {
                final var certificate = chain[i];
                if (!(certificate instanceof X509Certificate)) {
                    throw new IllegalStateException("Only x.509 " +
                        "certificates may be used by connecting clients, " +
                        "somehow the client at " + remoteSocketAddress() +
                        " was able to use some other type: " + certificate);
                }
                cachedCertificateChain[i] = (X509Certificate) chain[i];
            }
        }
        catch (final SSLPeerUnverifiedException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public InetSocketAddress remoteSocketAddress() {
        if (cachedRemoteSocketAddress == null) {
            cachedRemoteSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        }
        return cachedRemoteSocketAddress;
    }

    @Override
    public Optional<String> token() {
        if (!isTokenCached) {
            isTokenCached = true;
            cachedToken = headers.get("authorization");
        }
        return Optional.ofNullable(cachedToken);
    }
}
