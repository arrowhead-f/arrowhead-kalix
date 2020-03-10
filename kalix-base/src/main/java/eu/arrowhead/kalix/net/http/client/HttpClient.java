package eu.arrowhead.kalix.net.http.client;

import eu.arrowhead.kalix.util.concurrent.Future;

import java.net.InetSocketAddress;

/**
 * Client useful for sending HTTP requests to a single remote socket address.
 * <p>
 * HTTP clients are intended to be short-lived. When created, they should hold
 * live TCP connections through which messages can be exchanged until the
 * connection is closed, intentionally or not.
 */
public interface HttpClient {
    /**
     * @return Address of host reachable via this client.
     */
    InetSocketAddress remoteSocketAddress();

    /**
     * @return Local network interface bound to this client.
     */
    InetSocketAddress localSocketAddress();

    /**
     * @return {@code true} only if this client can be used to send request to
     * its remote peer.
     */
    boolean isLive();

    /**
     * Sends given {@code request} to HTTP service represented by this
     * {@code HttpClient}.
     *
     * @param request HTTP request to send.
     * @return Future of {@code HttpClientResponse}.
     */
    Future<HttpClientResponse> send(final HttpClientRequest request);

    /**
     * Attempts to close the client, destroying any connection with its remote
     * host.
     *
     * @return Future completed when closing is done.
     */
    Future<?> close();
}
