package se.arkalix.internal.net.http;

import se.arkalix.net.http.HttpPeer;
import se.arkalix.util.annotation.Internal;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.Objects;

@Internal
public class NettyHttpPeer implements HttpPeer {
    private final InetSocketAddress remoteSocketAddress;
    private final SSLEngine sslEngine;

    private X509Certificate[] cachedCertificateChain;

    /**
     * Creates new {@link HttpPeer}.
     *
     * @param remoteSocketAddress Internet socket address of peer.
     * @param sslEngine           SSL/TLS engine used by channel through which
     *                            the peer is communicated with, if any.
     */
    public NettyHttpPeer(final InetSocketAddress remoteSocketAddress, final SSLEngine sslEngine) {
        this.remoteSocketAddress = Objects.requireNonNull(remoteSocketAddress, "Expected remoteSocketAddress");
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
                        "somehow the peer at " + remoteSocketAddress +
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
        return remoteSocketAddress;
    }
}
