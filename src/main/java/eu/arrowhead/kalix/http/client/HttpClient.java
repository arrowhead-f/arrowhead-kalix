package eu.arrowhead.kalix.http.client;

import eu.arrowhead.kalix.concurrent.Future;

import java.net.InetSocketAddress;

/**
 * Client useful for sending HTTP requests to a single remote host.
 */
public class HttpClient {
    private final InetSocketAddress host;

    /**
     * Creates new {@link HttpClient} for communicating with {@code host}.
     *
     * @param host Hostname/port or IP/port of target host.
     */
    public HttpClient(final InetSocketAddress host) {
        this.host = host;
    }

    /**
     * @return Host reachable via this client
     */
    public InetSocketAddress host() {
        return host;
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
