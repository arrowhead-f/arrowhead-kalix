package se.arkalix.net.http.client;

import se.arkalix.ArSystem;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.descriptor.TransportDescriptor;
import se.arkalix.internal.net.http.client.FutureHttpClientConnection;
import se.arkalix.internal.net.http.client.NettyHttpClientConnectionInitializer;
import se.arkalix.security.identity.OwnedIdentity;
import se.arkalix.security.identity.TrustStore;
import se.arkalix.util.annotation.ThreadSafe;
import se.arkalix.util.concurrent.Schedulers;
import se.arkalix.util.concurrent.Future;
import se.arkalix.internal.util.concurrent.NettyScheduler;
import io.netty.bootstrap.Bootstrap;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import static se.arkalix.internal.util.concurrent.NettyFutures.adapt;

/**
 * Allows for the creation of TCP connections through which HTTP messages can
 * be sent.
 */
@SuppressWarnings("unused")
public class HttpClient {
    private static final Map<ArSystem, HttpClient> cache = new WeakHashMap<>();

    private static HttpClient http = null;
    private static HttpClient https = null;

    private final Bootstrap bootstrap;
    private final InetSocketAddress localSocketAddress;
    private final SslContext sslContext;

    private HttpClient(final Builder builder) throws SSLException {
        final var scheduler = (NettyScheduler) Schedulers.fixed();
        bootstrap = new Bootstrap()
            .group(scheduler.eventLoopGroup())
            .channel(scheduler.socketChannelClass());
        localSocketAddress = builder.localSocketAddress;

        if (builder.isInsecure) {
            sslContext = null;
        }
        else {
            final var sslContextBuilder = SslContextBuilder.forClient()
                .trustManager(builder.trustStore != null ? builder.trustStore.certificates() : null)
                .startTls(false);

            if (builder.identity != null) {
                sslContextBuilder
                    .keyManager(builder.identity.privateKey(), builder.identity.chain())
                    .clientAuth(ClientAuth.REQUIRE);
            }
            sslContext = sslContextBuilder.build();
        }
    }

    /**
     * Creates new or gets a cached {@code HttpClient} that takes its
     * configuration details from the given Arrowhead {@code system}.
     * <p>
     * The returned HTTP client will use the same owned identity, trust store,
     * security mode, local network interface and scheduler as the given
     * system, which makes it suitable for communicating with other systems
     * within the same local cloud.
     * <p>
     * If wanting to communicate with other Arrowhead systems without
     * creating an {@link ArSystem} instance, use the {@link Builder} instead.
     *
     * @param system Arrowhead system from which to extract configuration.
     * @return Created or cached client.
     * @throws SSLException If creating SSL/TLS context from given Arrowhead
     *                      system fails.
     */
    @ThreadSafe
    public synchronized static HttpClient from(final ArSystem system) throws SSLException {
        var client = cache.get(system);
        if (client != null) {
            return client;
        }

        final var builder = new Builder();
        if (system.isSecure()) {
            builder
                .identity(system.identity())
                .trustStore(system.trustStore());
        }
        else {
            builder.insecure();
        }

        client = builder
            .localSocketAddress(new InetSocketAddress(system.localAddress(), 0))
            .build();

        cache.put(system, client);
        return client;
    }

    /**
     * Creates new or gets a cached {@code HttpClient} with a default
     * configuration making it suitable for communicating with regular HTTP
     * servers without encryption.
     * <p>
     * The returned client is intended primarily for communicating with legacy
     * systems that are known to be trusted and where encryption is infeasible
     * to add. Its use is not advised for most kinds of production scenarios.
     * If wanting to communicate with other Arrowhead systems, please prefer
     * use of the {@link #from(ArSystem)} method for creating HTTP clients.
     *
     * @return New or cached HTTP client.
     */
    @ThreadSafe
    public synchronized static HttpClient http() {
        if (http == null) {
            try {
                http = new Builder()
                    .insecure()
                    .build();
            }
            catch (final SSLException exception) {
                throw new RuntimeException(exception);
            }
        }
        return http;
    }

    /**
     * Creates new or gets a cached {@code HttpClient} with a default
     * configuration making it suitable for communicating with regular HTTPS
     * servers.
     * <p>
     * The client will rely on the default trust manager that comes with the
     * system Java installation, if any, which tends to be useful for
     * interacting with just about any Internet HTTPS server.
     * <p>
     * The returned client is intended primarily for communicating with legacy
     * systems and/or traditional HTTPS servers. If wanting to communicate with
     * other Arrowhead systems, please prefer use of the
     * {@link #from(ArSystem)} method for creating HTTP clients.
     *
     * @return New or cached HTTPS client.
     */
    @ThreadSafe
    public synchronized static HttpClient https() throws SSLException {
        if (https == null) {
            https = new Builder().build();
        }
        return https;
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
     * Creates new {@code HttpClientConnection} for consuming the described
     * {@code remoteService}.
     * <p>
     * The returned future is completed with an {@link IllegalStateException}
     * if the details of the described {@code remoteService} do not match this
     * client.
     *
     * @param remoteService Remote service to consume.
     * @return Future completed with a new client if and when a TCP connection
     * has been established to the remote socket address of the system
     * providing the {@code remoteService}.
     */
    public Future<HttpClientConnection> connect(final ServiceDescription remoteService) {
        return connect(remoteService, null);
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
     * Creates new {@code HttpClientConnection} for consuming the described
     * {@code remoteService} via the specified {@code localSocketAddress}.
     * <p>
     * The returned future is completed with an {@link IllegalStateException}
     * if the details of the described {@code remoteService} do not match this
     * client.
     *
     * @param remoteService      Remote service to consume.
     * @param localSocketAddress Socket address of local network interface to
     *                           use when communicating with the service
     *                           provider.
     * @return Future completed with a new client if and when a TCP connection
     * has been established to the remote socket address of the system
     * providing the {@code remoteService}.
     */
    public Future<HttpClientConnection> connect(
        final ServiceDescription remoteService,
        final InetSocketAddress localSocketAddress)
    {
        Objects.requireNonNull(remoteService, "Expected remoteService");

        final var remoteIsSecure = remoteService.security() != SecurityDescriptor.NOT_SECURE;
        final var clientIsSecure = sslContext != null;

        if (!remoteIsSecure && clientIsSecure) {
            return Future.failure(new IllegalStateException("Cannot create " +
                "secure HTTP connection to \"" + remoteService.name() + "\"" +
                "; the service is running in insecure mode"));
        }
        if (remoteIsSecure && !clientIsSecure) {
            return Future.failure(new IllegalStateException("Cannot create " +
                "insecure HTTP connection to \"" + remoteService.name() + "\"" +
                "; the service is running in secure mode"));
        }

        final var isHttpSupported = remoteService.interfaces()
            .stream().anyMatch(triplet -> triplet.isSecure() == clientIsSecure &&
                triplet.transport() == TransportDescriptor.HTTP);

        if (!isHttpSupported) {
            return Future.failure(new IllegalStateException("Cannot create " +
                "HTTP connection to \"" + remoteService.name() + "\"; the " +
                "service does not support HTTP" + (clientIsSecure ? "S" : "")));
        }

        return connect(remoteService.provider().socketAddress(), localSocketAddress);
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
     * Connects to the system providing the described {@code remoteService},
     * sends {@code request}, closes connection and then completes the returned
     * {@code Future} with the result.
     *
     * @param remoteService Service to consume.
     * @param request       Request to send.
     * @return Future completed with the request response or an error.
     * @throws NullPointerException If {@code remoteService} or {@code request}
     *                              is {@code null}.
     */
    public Future<HttpClientResponse> send(final ServiceDescription remoteService, final HttpClientRequest request) {
        Objects.requireNonNull(request, "Expected request");
        return connect(remoteService)
            .flatMap(connection -> connection.sendAndClose(request));
    }

    /**
     * Builder useful for creating {@link HttpClient} instances.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private InetSocketAddress localSocketAddress;
        private OwnedIdentity identity;
        private TrustStore trustStore;
        private boolean isInsecure = false;

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
         * Sets owned identity to use for representing created HTTP clients.
         * <p>
         * If insecure mode is not enabled and no identity is provided, client
         * authentication is disabled for created HTTP clients, making them
         * unsuitable for communicating with Arrowhead systems. The clients
         * may, however, be used for communicating with arbitrary HTTP servers
         * that do not require client authentication.
         *
         * @param identity Owned identity to use.
         * @return This builder.
         */
        public final Builder identity(final OwnedIdentity identity) {
            this.identity = identity;
            return this;
        }

        /**
         * Sets trust store to use for determining what systems are trusted to
         * be communicated with by created HTTP clients.
         * <p>
         * If insecure mode is not enabled and no trust store is provided, the
         * default system trust store is used instead. This is typically
         * suitable if wanting to communicate with regular HTTP servers over
         * HTTPS.
         *
         * @param trustStore Trust store to use.
         * @return This builder.
         */
        public final Builder trustStore(final TrustStore trustStore) {
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
         * @return New {@link HttpClient}.
         */
        public HttpClient build() throws SSLException {
            return new HttpClient(this);
        }
    }
}
