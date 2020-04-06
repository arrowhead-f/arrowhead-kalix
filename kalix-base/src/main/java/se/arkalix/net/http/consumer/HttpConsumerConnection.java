package se.arkalix.net.http.consumer;

import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.net.http.client.HttpClientConnection;
import se.arkalix.security.identity.SystemIdentity;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;

/**
 * Connection useful for sending HTTP requests to a consumed service.
 */
public class HttpConsumerConnection {
    private final HttpClientConnection connection;
    private final EncodingDescriptor encoding;
    private final SystemIdentity provider;


    HttpConsumerConnection(
        final HttpClientConnection connection,
        final EncodingDescriptor encoding,
        final SystemIdentity provider)
    {
        this.connection = connection;
        this.encoding = encoding;
        this.provider = provider;
    }

    /**
     * @return Local network interface bound to this connection.
     */
    public InetSocketAddress localSocketAddress() {
        return connection.localSocketAddress();
    }

    /**
     * @return Identity associated with the provider of the consumed service.
     * @throws IllegalStateException If the provider is not running in secure
     *                               mode.
     */
    public SystemIdentity provider() {
        return provider;
    }

    /**
     * @return {@code true} only if this connection can be used to send
     * requests to the service provider.
     */
    public boolean isLive() {
        return connection.isLive();
    }

    /**
     * @return {@code true} only if the provider of the consumed service is
     * running in secure mode, which implies that the connection is secured via
     * HTTPS.
     */
    public boolean isSecure() {
        return connection.isSecure();
    }

    /**
     * Sends given {@code request} to HTTP service represented by this
     * {@code HttpClientConnection}.
     *
     * @param request HTTP request to send.
     * @return {@code Future} {@code HttpConsumerResponse}.
     */
    public Future<HttpConsumerResponse> send(final HttpConsumerRequest request) {
        return connection.send(request.asClientRequest())
            .map(response -> new HttpConsumerResponse(response, encoding));
    }

    /**
     * Sends given {@code request} to HTTP service represented by this
     * {@code HttpClientConnection}, awaits either a response or an error,
     * closes this connection and then completes the returned {@code Future}
     * with the resulting response or error.
     *
     * @param request HTTP request to send.
     * @return {@code Future} {@code HttpConsumerResponse}.
     */
    public Future<HttpConsumerResponse> sendAndClose(final HttpConsumerRequest request) {
        return connection.sendAndClose(request.asClientRequest())
            .map(response -> new HttpConsumerResponse(response, encoding));
    }

    /**
     * Attempts to close connection.
     *
     * @return Future completed when closing is done. Can be safely ignored.
     */
    public Future<?> close() {
        return connection.close();
    }
}
