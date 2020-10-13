package se.arkalix.net.http.consumer;

import se.arkalix.net.http.HttpConnectionWithArSystem;
import se.arkalix.util.concurrent.Future;

/**
 * Represents an established HTTP connection useful for consuming a remote
 * Arrowhead service.
 */
public interface HttpConsumerConnection extends HttpConnectionWithArSystem {
    /**
     * Sends given {@code request} to connected remote host.
     *
     * @param request HTTP request to send.
     * @return Future eventually completed with response or exception.
     */
    Future<HttpConsumerResponse> send(final HttpConsumerRequest request);

    /**
     * Sends given {@code request} to connected remote host and then closes
     * the connection after the response has either been received or an
     * exception prevents it from being received.
     *
     * @param request HTTP request to send.
     * @return Future eventually completed with response or exception.
     */
    Future<HttpConsumerResponse> sendAndClose(final HttpConsumerRequest request);
}
