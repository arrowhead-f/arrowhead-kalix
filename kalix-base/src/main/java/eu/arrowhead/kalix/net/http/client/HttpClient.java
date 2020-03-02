package eu.arrowhead.kalix.net.http.client;

import eu.arrowhead.kalix.util.concurrent.Future;

import java.net.InetSocketAddress;

/**
 * Client useful for sending HTTP requests to a single remote socket address.
 */
public class HttpClient {
    private final InetSocketAddress socketAddress;

    /**
     * Creates new {@link HttpClient} for communicating with given
     * {@code socketAddress}.
     *
     * @param socketAddress Hostname/port or IP/port of target socketAddress.
     */
    public HttpClient(final InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    /**
     * @return Host reachable via this client.
     */
    public InetSocketAddress socketAddress() {
        return socketAddress;
    }

    /**
     * Sends given {@code request} to HTTP service represented by this
     * {@code HttpClient}.
     *
     * @param request HTTP request to send.
     * @return Future of {@code HttpClientResponse}.
     */
    public Future<HttpClientResponse> send(final HttpClientRequest request) {
        return Future.failure(new UnsupportedOperationException()); // TODO.
    }
}
