package eu.arrowhead.kalix.net.http.client;

import eu.arrowhead.kalix.net.http.HttpPeer;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.net.InetSocketAddress;

/**
 * Client useful for sending HTTP requests to a single remote socket address.
 * <p>
 * HTTP clients are intended to be short-lived. When created, they should hold
 * live TCP connections through which messages can be exchanged until the
 * connection is closed, intentionally or not.
 */
public interface HttpClientConnection extends HttpPeer {
    /**
     * @return Address of host reachable via this connection.
     */
    InetSocketAddress remoteSocketAddress();

    /**
     * @return Local network interface bound to this connection.
     */
    InetSocketAddress localSocketAddress();

    /**
     * @return {@code true} only if this connection can be used to send
     * requests to its remote peer.
     */
    boolean isLive();

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
     * Attempts to close the client, destroying any connection with its remote
     * host.
     *
     * @return Future completed when closing is done. Can be safely ignored.
     */
    Future<?> close();
}
