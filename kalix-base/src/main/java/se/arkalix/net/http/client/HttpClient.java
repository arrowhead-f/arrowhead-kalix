package se.arkalix.net.http.client;

import se.arkalix.ArSystem;
import se.arkalix.internal.net.NettyBootstraps;
import se.arkalix.internal.net.http.client.FutureHttpClientConnection;
import se.arkalix.internal.net.http.client.NettyHttpClientConnectionInitializer;
import se.arkalix.security.identity.ArSystemKeyStore;
import se.arkalix.security.identity.ArTrustStore;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.FutureScheduler;
import io.netty.bootstrap.Bootstrap;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import static se.arkalix.internal.util.concurrent.NettyFutures.adapt;

/**
 * Allows for the creation of TCP connections through which HTTP messages can
 * be sent.
 */
public class HttpClient {
    private static final Map<ArSystem, HttpClient> cache = Collections.synchronizedMap(new WeakHashMap<>());

    private final Bootstrap bootstrap;
    private final InetSocketAddress localSocketAddress;
    private final SslContext sslContext;

    private HttpClient(final Builder builder) throws SSLException {
        bootstrap = NettyBootstraps.createBootstrapUsing(builder.scheduler != null
            ? builder.scheduler
            : FutureScheduler.getDefault());
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
                    .keyManager(builder.keyStore.privateKey(), builder.keyStore.systemChain())
                    .clientAuth(ClientAuth.REQUIRE);
            }
            sslContext = sslContextBuilder.build();
        }
    }

    /**
     * Creates new or gets a cached {@code HttpClient} that takes its
     * configuration details from the given Arrowhead {@code system}.
     * <p>
     * The return HTTP client will use the same key store, trust store,
     * security mode, local network interface and scheduler as the given
     * system.
     *
     * @param system Arrowhead system from which to extract configuration.
     * @return Created or cached client.
     * @throws SSLException If creating SSL/TLS context from given Arrowhead
     *                      system fails.
     */
    public static HttpClient from(final ArSystem system) throws SSLException {
        var client = cache.get(system);
        if (client != null) {
            return client;
        }

        final var builder = new Builder();
        if (system.isSecure()) {
            builder
                .keyStore(system.keyStore())
                .trustStore(system.trustStore());
        }
        else {
            builder.insecure();
        }

        client = builder
            .localSocketAddress(new InetSocketAddress(system.localAddress(), 0))
            .scheduler(system.scheduler())
            .build();

        cache.put(system, client);
        return client;
    }

    /**
     * Creates new {@code HttpClientConnection} for communicating with remote
     * host reachable via provided Internet socket address.
     *
     * @param remoteSocketAddress Remote socket address.
     * @return Future completed with a new client if and when a TCP connection
     * has been established to {@code remoteSocketAddress}.
     * @throws NullPointerException If {@code remoteSocketAddress} is
     *                              {@code null}.
     */
    public Future<HttpClientConnection> connect(final InetSocketAddress remoteSocketAddress) {
        return connect(remoteSocketAddress, null);
    }

    /**
     * Creates new {@code HttpClientConnection} for communicating with remote
     * host reachable via provided Internet socket address.
     *
     * @param remoteSocketAddress Remote socket address.
     * @param localSocketAddress  Socket address of local network interface to
     *                            use when communicating with remote host.
     * @return Future completed with a new client if and when a TCP connection
     * has been established to {@code remoteSocketAddress}.
     * @throws NullPointerException If {@code remoteSocketAddress} is
     *                              {@code null}.
     */
    public Future<HttpClientConnection> connect(
        final InetSocketAddress remoteSocketAddress,
        final InetSocketAddress localSocketAddress)
    {
        Objects.requireNonNull(remoteSocketAddress, "Expected remoteSocketAddress");

        final var futureConnection = new FutureHttpClientConnection();
        return adapt(bootstrap.clone()
            .handler(new NettyHttpClientConnectionInitializer(futureConnection, sslContext))
            .connect(remoteSocketAddress, localSocketAddress != null
                ? localSocketAddress
                : this.localSocketAddress))
            .flatMap(ignored -> futureConnection);
    }

    /**
     * Connects to remote host at {@code remoteSocketAddress}, sends
     * {@code request}, closes connection and then completes the returned
     * {@code Future} with the result.
     *
     * @param remoteSocketAddress Remote socket address.
     * @param request             Request to send.
     * @return Future completed with the request response or an error.
     * @throws NullPointerException If {@code remoteSocketAddress} or
     *                              {@code request} is {@code null}.
     */
    public Future<HttpClientResponse> send(
        final InetSocketAddress remoteSocketAddress,
        final HttpClientRequest request)
    {
        Objects.requireNonNull(request, "Expected request");
        return connect(remoteSocketAddress)
            .flatMap(connection -> connection.sendAndClose(request));
    }

    /**
     * Builder useful for creating {@link HttpClient} instances.
     */
    public static class Builder {
        private InetSocketAddress localSocketAddress;
        private ArSystemKeyStore keyStore;
        private ArTrustStore trustStore;
        private boolean isInsecure = false;
        private FutureScheduler scheduler;

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
        public final Builder keyStore(final ArSystemKeyStore keyStore) {
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
        public final Builder trustStore(final ArTrustStore trustStore) {
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
         * @return New {@link HttpClient}.
         */
        public HttpClient build() throws SSLException {
            return new HttpClient(this);
        }
    }
}
