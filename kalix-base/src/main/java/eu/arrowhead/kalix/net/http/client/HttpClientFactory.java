package eu.arrowhead.kalix.net.http.client;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import eu.arrowhead.kalix.internal.net.NettyBootstraps;
import eu.arrowhead.kalix.internal.net.http.client.HttpResponseReceiver;
import eu.arrowhead.kalix.internal.net.http.client.NettyHttpClient;
import eu.arrowhead.kalix.internal.net.http.client.NettyHttpClientConnectionInitializer;
import eu.arrowhead.kalix.security.X509KeyStore;
import eu.arrowhead.kalix.security.X509TrustStore;
import eu.arrowhead.kalix.util.concurrent.Future;
import eu.arrowhead.kalix.util.concurrent.FutureScheduler;
import io.netty.bootstrap.Bootstrap;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.Objects;

import static eu.arrowhead.kalix.internal.util.concurrent.NettyFutures.adapt;

/**
 * A factory for creating {@link HttpClient} instances.
 */
public class HttpClientFactory {
    private final Bootstrap bootstrap;
    private final EncodingDescriptor[] encodings;
    private final InetSocketAddress localSocketAddress;
    private final SslContext sslContext;

    private HttpClientFactory(final Builder builder) throws SSLException {
        bootstrap = NettyBootstraps.createBootstrapUsing(builder.scheduler != null
            ? builder.scheduler
            : FutureScheduler.getDefault());
        encodings = Objects.requireNonNull(builder.encodings, "Expected encodings");
        if (encodings.length == 0) {

        }
        localSocketAddress = builder.localSocketAddress;
        if (builder.isInsecure) {
            sslContext = null;
        }
        else {
            final var sslContextBuilder = SslContextBuilder.forClient()
                .trustManager(builder.trustStore != null ? builder.trustStore.certificates() : null)
                .startTls(false);

            if (builder.keyStore != null) {
                sslContextBuilder
                    .keyManager(builder.keyStore.privateKey(), builder.keyStore.certificateChain())
                    .clientAuth(ClientAuth.REQUIRE);
            }
            sslContext = sslContextBuilder.build();
        }
    }

    /**
     * Creates new {@code HttpClient} for communicating with remote host
     * reachable via provided Internet socket address.
     *
     * @param remoteSocketAddress Remote socket address.
     * @return Future completed with a new client if and when a TCP connection
     * has been established to {@code remoteSocketAddress}.
     * @throws NullPointerException If {@code remoteSocketAddress} is
     *                              {@code null}.
     */
    public Future<HttpClient> createFor(final InetSocketAddress remoteSocketAddress) {
        return createFor(remoteSocketAddress, null);
    }

    /**
     * Creates new {@code HttpClient} for communicating with remote host
     * reachable via provided Internet socket address.
     *
     * @param remoteSocketAddress Remote socket address.
     * @param localSocketAddress  Socket address of local network interface to
     *                            use when communicating with remote host.
     * @return Future completed with a new client if and when a TCP connection
     * has been established to {@code remoteSocketAddress}.
     * @throws NullPointerException If {@code remoteSocketAddress} is
     *                              {@code null}.
     */
    public Future<HttpClient> createFor(
        final InetSocketAddress remoteSocketAddress,
        InetSocketAddress localSocketAddress)
    {
        Objects.requireNonNull(remoteSocketAddress);
        if (localSocketAddress == null) {
            localSocketAddress = this.localSocketAddress;
        }

        final var client = new NettyHttpClient();
        final var responseReceiver = new HttpResponseReceiver() {
            @Override
            public EncodingDescriptor[] encodings() {
                return encodings;
            }

            @Override
            public void receive(final HttpClientResponse response) {
                client.receive(response);
            }

            @Override
            public void fail(final Throwable throwable) {
                // TODO
            }
        };
        return adapt(bootstrap.clone()
            .handler(new NettyHttpClientConnectionInitializer(responseReceiver, sslContext))
            .connect(remoteSocketAddress, localSocketAddress))
            .map(channel -> {
                client.setChannel(channel);
                return client;
            });
    }

    /**
     * Builder useful for creating {@link HttpClientFactory} instances.
     */
    public static class Builder {
        private EncodingDescriptor[] encodings;
        private InetSocketAddress localSocketAddress;
        private X509KeyStore keyStore;
        private X509TrustStore trustStore;
        private boolean isInsecure = false;
        private FutureScheduler scheduler;

        /**
         * Declares what data encodings this clients created with this factory
         * can read and write. <b>Must be specified.</b>
         * <p>
         * While the created {@link HttpClientFactory} will prevent messages
         * claimed to be encoded with other encodings from being received,
         * stating that an encoding can be read and written does not itself
         * guarantee it. It is up to the {@link HttpClientFactory} creator to
         * ensure that such capabilities are indeed available. For most intents
         * and purposes, the most adequate way of achieving this is by using
         * Data Transfer Objects (DTOs), more of which you can read about in
         * the package documentation for the {@code eu.arrowhead.kalix.dto}
         * package.
         *
         * @param encodings Encodings declared to be supported. At least one
         *                  must be provided.
         * @return This builder.
         * @see eu.arrowhead.kalix.dto
         */
        public final Builder encodings(final EncodingDescriptor... encodings) {
            this.encodings = encodings.clone();
            return this;
        }

        /**
         * Ensures that the identified local network interface is used by
         * created HTTP clients by default.
         *
         * @param socketAddress Internet socket address associated with the
         *                      preferred local network interface.
         * @return This builder.
         */
        public final Builder localSocketAddress(final InetSocketAddress socketAddress) {
            this.localSocketAddress = socketAddress;
            return this;
        }

        /**
         * Sets key store to use for representing created HTTP clients.
         * <p>
         * If insecure mode is not enabled and no key store is provided, client
         * authentication is disabled for created HTTP clients, making them
         * unsuitable for communicating with Arrowhead systems. The clients
         * may, however, be used for communicating with arbitrary HTTP servers
         * that do not require client authentication.
         *
         * @param keyStore Key store to use.
         * @return This builder.
         */
        public final Builder keyStore(final X509KeyStore keyStore) {
            this.keyStore = keyStore;
            return this;
        }

        /**
         * Sets trust store to use for determining what systems are trusted to
         * be communicated with by created HTTP clients.
         * <p>
         * If insecure mode is not enabled and no trust store is provided, the
         * default system trust store is used instead.
         *
         * @param trustStore Trust store to use.
         * @return This builder.
         */
        public final Builder trustStore(final X509TrustStore trustStore) {
            this.trustStore = trustStore;
            return this;
        }

        /**
         * Explicitly enables insecure mode for this client.
         * <p>
         * In insecure mode, no cryptography is used to establish identities or
         * connections between systems. Usage of this mode is not advised for
         * most kinds of production scenarios.
         *
         * @return This builder.
         */
        public final Builder insecure() {
            this.isInsecure = true;
            return this;
        }

        /**
         * Sets scheduler to be used by created HTTP clients.
         * <p>
         * If a non-null scheduler is explicitly provided via this method, it
         * becomes the responsibility of the caller to ensure that the
         * scheduler is shut down when no longer in use.
         * <p>
         * If no scheduler is explicitly specified, the one returned by
         * {@link FutureScheduler#getDefault()} is used instead. This means
         * that if several factories are created without schedulers being
         * explicitly set, the factories will all share the same schedulers.
         *
         * @param scheduler Asynchronous task scheduler.
         * @return This builder.
         */
        public final Builder scheduler(final FutureScheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        /**
         * @return New {@link HttpClientFactory}.
         */
        public HttpClientFactory build() throws SSLException {
            return new HttpClientFactory(this);
        }
    }
}
