package se.arkalix.net.http.client._internal;

import io.netty.bootstrap.Bootstrap;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import se.arkalix.ArSystem;
import se.arkalix.security.identity._internal.X509Certificates;
import se.arkalix.util.concurrent._internal.FutureCompletion;
import se.arkalix.util.concurrent._internal.NettyScheduler;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientConnection;
import se.arkalix.security.SecurityException;
import se.arkalix.security.identity.OwnedIdentity;
import se.arkalix.security.identity.TrustStore;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.annotation.ThreadSafe;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Schedulers;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;

import static se.arkalix.util.concurrent._internal.NettyFutures.adapt;

@Internal
public class NettyHttpClient implements HttpClient {
    private static final Map<ArSystem, NettyHttpClient> cache = new WeakHashMap<>();

    private static NettyHttpClient http = null;
    private static NettyHttpClient https = null;

    private final Bootstrap bootstrap;
    private final InetSocketAddress localSocketAddress;
    private final SslContext sslContext;

    public NettyHttpClient(final Builder builder) {
        final var scheduler = (NettyScheduler) Schedulers.fixed();
        bootstrap = new Bootstrap()
            .group(scheduler.eventLoopGroup())
            .channel(scheduler.socketChannelClass());
        localSocketAddress = builder.localSocketAddress;

        if (builder.isSecure) {
            final var sslContextBuilder = SslContextBuilder.forClient()
                .trustManager(builder.trustedCertificates != null && builder.trustedCertificates.length > 0
                    ? X509Certificates.castOrThrow(builder.trustedCertificates, (index, certificate) ->
                    new IllegalArgumentException("Certificate at index " +
                        index + " of given trust store is not a " +
                        "X509Certificate; cannot use trust store"))
                    : null)
                .startTls(false);

            if (builder.certificateChain != null) {
                if (builder.privateKey == null) {
                    throw new IllegalArgumentException("A certificate chain was provided without a private key");
                }
                sslContextBuilder
                    .keyManager(
                        builder.privateKey,
                        X509Certificates.castOrThrow(builder.certificateChain, (index, certificate) ->
                            new IllegalArgumentException("Certificate at index " +
                                index + " of given certificate chain is not a " +
                                "X509Certificate; cannot use certificate chain")))
                    .clientAuth(ClientAuth.REQUIRE);
            }
            else if (builder.privateKey != null) {
                throw new IllegalArgumentException("A private key was provided without a certificate chain");
            }
            try {
                sslContext = sslContextBuilder.build();
            }
            catch (final SSLException exception) {
                throw new SecurityException("Failed to create SSL context for HttpClient", exception);
            }
        }
        else {
            if (builder.certificateChain != null) {
                throw new IllegalArgumentException("HTTP client using secure transport; certificate chain not expected");
            }
            if (builder.privateKey != null) {
                throw new IllegalArgumentException("HTTP client using secure transport; private key not expected");
            }
            if (builder.trustedCertificates != null) {
                throw new IllegalArgumentException("HTTP client using secure transport; trust store not expected");
            }
            sslContext = null;
        }
    }

    @ThreadSafe
    public synchronized static NettyHttpClient from(final ArSystem system) {
        var client = cache.get(system);
        if (client != null) {
            return client;
        }

        final var builder = new Builder();
        if (system.isSecure()) {
            builder.identity(system.identity());
            builder.trustStore(system.trustStore());
        }
        else {
            builder.insecure();
        }

        builder.localSocketAddress(new InetSocketAddress(system.address(), 0));
        client = builder.build();

        cache.put(system, client);
        return client;
    }

    @Override
    public boolean isSecure() {
        return sslContext != null;
    }

    @Override
    public Optional<InetSocketAddress> localSocketAddress() {
        return Optional.ofNullable(localSocketAddress);
    }

    @ThreadSafe
    public synchronized static NettyHttpClient insecure() {
        if (http == null) {
            final var builder = new Builder();
            http = builder.build();
        }
        return http;
    }

    @ThreadSafe
    public synchronized static NettyHttpClient secure() {
        if (https == null) {
            https = new Builder().build();
        }
        return https;
    }

    @Override
    public Future<HttpClientConnection> connect(
        final InetSocketAddress remoteSocketAddress,
        final InetSocketAddress localSocketAddress
    ) {
        Objects.requireNonNull(remoteSocketAddress, "remoteSocketAddress");

        final var futureConnection = new FutureCompletion<HttpClientConnection>();
        return adapt(bootstrap.clone()
            .handler(new NettyHttpClientConnectionInitializer(futureConnection, sslContext))
            .connect(remoteSocketAddress, localSocketAddress != null
                ? localSocketAddress
                : this.localSocketAddress))
            .flatMap(ignored -> futureConnection);
    }

    public static class Builder {
        private InetSocketAddress localSocketAddress;
        private Certificate[] certificateChain;
        private PrivateKey privateKey;
        private Certificate[] trustedCertificates;
        private boolean isSecure = true;

        public final void localSocketAddress(final InetSocketAddress socketAddress) {
            this.localSocketAddress = socketAddress;
        }

        public final void identity(final OwnedIdentity identity) {
            identity(identity.chain(), identity.privateKey());
        }

        public final void identity(final Certificate[] certificateChain, final PrivateKey privateKey) {
            this.certificateChain = certificateChain;
            this.privateKey = privateKey;
        }

        public final void trustStore(final TrustStore trustStore) {
            trustStore(trustStore.certificates());
        }

        public final void trustStore(final Certificate[] trustedCertificates) {
            this.trustedCertificates = trustedCertificates;
        }

        public final void insecure() {
            this.isSecure = false;
        }

        public NettyHttpClient build() {
            return new NettyHttpClient(this);
        }
    }
}
