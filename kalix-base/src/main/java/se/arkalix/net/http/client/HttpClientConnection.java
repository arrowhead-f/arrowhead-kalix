package se.arkalix.net.http.client;

import se.arkalix.net.http.HttpConnection;
import se.arkalix.util.concurrent.Future;

/**
 * Represents an HTTP connection established between a local {@link HttpClient}
 * and some remote HTTP server.
 */
public interface HttpClientConnection extends HttpConnection {
    /**
     * Sends given {@code request} to connected remote host.
     *
     * @param request HTTP request to send.
     * @return Future eventually completed with response or exception.
     */
    Future<HttpClientResponse> send(final HttpClientRequest request);

    /**
     * Sends given {@code request} to connected remote host and then closes
     * the connection after the response has either been received or an
     * exception prevents it from being received.
     *
     * @param request HTTP request to send.
     * @return Future eventually completed with response or exception.
     */
    Future<HttpClientResponse> sendAndClose(final HttpClientRequest request);
}
