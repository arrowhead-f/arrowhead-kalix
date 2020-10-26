package se.arkalix.net.http.client;

import se.arkalix.ArSystem;
import se.arkalix.net.http.client._internal.NettyHttpClient;
import se.arkalix.net.http.consumer._internal.DefaultHttpConsumer;
import se.arkalix.security.identity.OwnedIdentity;
import se.arkalix.security.identity.TrustStore;
import se.arkalix.util.annotation.ThreadSafe;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Objects;
import java.util.Optional;

/**
 * Client useful for sending HTTP messages via TCP connections to arbitrary
 * remote hosts.
 */
public interface HttpClient {
    /**
     * Creates new or gets a cached {@code HttpClient} that takes its
     * configuration details from the given Arrowhead {@code system}.
     * <p>
     * The returned HTTP client will use the same owned identity, trust store,
     * security mode and local network interface as the given system, which
     * makes it suitable for communicating with other systems within the same
     * local cloud. However, for most intents and purposes it is preferable to
     * use the {@link DefaultHttpConsumer HttpConsumer}
     * class for consuming system services, as it takes care of keeping track
     * of IP addresses, authorization tokens and other relevant details. Such
     * an instance can with advantage be constructed from the successful result
     * of the {@link ArSystem#consume()} method.
     * <p>
     * If wanting to communicate with other Arrowhead systems without
     * creating an {@link ArSystem} instance, use the {@link Builder} instead.
     *
     * @param system Arrowhead system from which to extract configuration.
     * @return Created or cached client.
     */
    @ThreadSafe
    static HttpClient from(final ArSystem system) {
        return NettyHttpClient.from(system);
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
    static HttpClient insecure() {
        return NettyHttpClient.insecure();
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
    static HttpClient secure() {
        return NettyHttpClient.secure();
    }

    /**
     * Determines whether or not this HTTP client communicates via HTTPS or not.
     *
     * @return {@code true} only if this client uses HTTPS.
     */
    boolean isSecure();

    /**
     * Gets the socket address of the network interface configured to be used
     * by default when making new connections, if any.
     *
     * @return Default local socket address, if any.
     */
    Optional<InetSocketAddress> localSocketAddress();

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
    default Future<HttpClientConnection> connect(final InetSocketAddress remoteSocketAddress) {
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
    Future<HttpClientConnection> connect(
        final InetSocketAddress remoteSocketAddress,
        final InetSocketAddress localSocketAddress
    );

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
    default Future<HttpClientResponse> send(
        final InetSocketAddress remoteSocketAddress,
        final HttpClientRequest request
    ) {
        Objects.requireNonNull(request, "Expected request");
        return connect(remoteSocketAddress, localSocketAddress().orElse(null))
            .flatMap(connection -> connection.sendAndClose(request));
    }

    /**
     * Builder useful for creating {@link HttpClient} instances.
     */
    @SuppressWarnings("UnusedReturnValue")
    class Builder {
        private final NettyHttpClient.Builder inner = new NettyHttpClient.Builder();

        /**
         * Ensures that the identified local network interface is used by
         * created HTTP clients by default.
         *
         * @param socketAddress Internet socket address associated with the
         *                      preferred local network interface.
         * @return This builder.
         */
        public final Builder localSocketAddress(final InetSocketAddress socketAddress) {
            inner.localSocketAddress(socketAddress);
            return this;
        }

        /**
         * Sets owned certificate chain and corresponding private key to use
         * for representing created HTTP clients from given owned identity
         * object.
         * <p>
         * If {@link #insecure() insecure mode} is <i>not</i> enabled and no
         * identity is provided, HTTPS is still enabled. However, client
         * authentication is disabled for created clients, making them
         * unsuitable for communicating with Arrowhead systems. The clients
         * may, however, be used for communicating with arbitrary HTTP servers
         * that do not require client authentication.
         *
         * @param identity Owned identity to use.
         * @return This builder.
         * @see se.arkalix.security Arrowhead Security
         */
        public final Builder identity(final OwnedIdentity identity) {
            return identity != null
                ? identity(identity.chain(), identity.privateKey())
                : identity(null, null);
        }

        /**
         * Sets owned certificate chain and corresponding private key to use
         * for representing created HTTP clients.
         * <p>
         * If {@link #insecure() insecure mode} is <i>not</i> enabled and no
         * identity is provided, HTTPS is still enabled. However, client
         * authentication is disabled for created clients, making them
         * unsuitable for communicating with Arrowhead systems. The clients
         * may, however, be used for communicating with arbitrary HTTP servers
         * that do not require client authentication.
         *
         * @param certificateChain Certificate chain to be used by created
         *                         clients.
         * @param privateKey       Private key associated with the certificate
         *                         at index 0 in the given certificate chain.
         * @return This builder.
         * @see se.arkalix.security Arrowhead Security
         */
        public final Builder identity(final Certificate[] certificateChain, final PrivateKey privateKey) {
            inner.identity(certificateChain, privateKey);
            return this;
        }

        /**
         * Sets trust store to use for determining what systems are trusted to
         * be communicated with by created HTTP clients.
         * <p>
         * If {@link #insecure() insecure mode} is <i>not</i> enabled and no
         * trust store is provided, the default system trust store is used
         * instead. This is typically suitable if wanting to communicate with
         * regular HTTP servers over HTTPS.
         *
         * @param trustStore Trust store to use.
         * @return This builder.
         * @see se.arkalix.security Arrowhead Security
         */
        public final Builder trustStore(final TrustStore trustStore) {
            return trustStore(trustStore != null
                ? trustStore.certificates()
                : null);
        }

        /**
         * Sets trust store to use for determining what systems are trusted to
         * be communicated with by created HTTP clients.
         * <p>
         * If {@link #insecure() insecure mode} is <i>not</i> enabled and no
         * trust store is provided, the default system trust store is used
         * instead. This is typically suitable if wanting to communicate with
         * regular HTTP servers over HTTPS.
         *
         * @param trustedCertificates Certificates to make up trust store used
         *                            by created clients.
         * @return This builder.
         * @see se.arkalix.security Arrowhead Security
         */
        public final Builder trustStore(final Certificate[] trustedCertificates) {
            inner.trustStore(trustedCertificates);
            return this;
        }

        /**
         * Explicitly enables insecure mode for this client.
         * <p>
         * In {@link se.arkalix.security insecure mode}, no cryptography is
         * used to establish identities or connections between systems. Usage
         * of this mode is not advised for most kinds of production scenarios.
         *
         * @return This builder.
         * @see se.arkalix.security Arrowhead Security
         */
        public final Builder insecure() {
            inner.insecure();
            return this;
        }

        /**
         * @return New {@link HttpClient}.
         */
        public HttpClient build() {
            return new NettyHttpClient(inner);
        }
    }
}
