package se.arkalix.net.http.client;

import se.arkalix.security.NotSecureException;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.security.cert.Certificate;

/**
 * Connection, established via a {@link HttpClient}, useful for sending HTTP
 * requests to a single remote socket address.
 */
public interface HttpClientConnection {
    /**
     * @return Address of host reachable via this connection.
     */
    InetSocketAddress remoteSocketAddress();

    /**
     * @return Local network interface bound to this connection.
     */
    InetSocketAddress localSocketAddress();

    /**
     * @return Certificate chain associated with host reachable via this
     * connection.
     * @throws NotSecureException If the connection is not secure.
     */
    Certificate[] certificateChain();

    /**
     * @return {@code true} only if this connection can be used to send
     * requests to its remote peer.
     */
    boolean isLive();

    /**
     * @return {@code true} only if this is an HTTPS connection.
     */
    boolean isSecure();

    /**
     * Sends given {@code request} to HTTP service represented by this
     * {@code HttpClientConnection}.
     *
     * @param request HTTP request to send.
     * @return Future of {@code HttpClientResponse}.
     */
    Future<HttpClientResponse> send(final HttpClientRequest request);

    /**
     * Sends given {@code request} to HTTP service represented by this
     * {@code HttpClientConnection}, awaits either a response or an error,
     * closes this connection and then completes the returned {@code Future}
     * with the resulting response or error.
     *
     * @param request HTTP request to send.
     * @return Future of {@code HttpClientResponse}.
     */
    Future<HttpClientResponse> sendAndClose(final HttpClientRequest request);

    /**
     * Attempts to close the connection.
     *
     * @return Future completed when closing is done. Can be safely ignored.
     */
    Future<?> close();
}
