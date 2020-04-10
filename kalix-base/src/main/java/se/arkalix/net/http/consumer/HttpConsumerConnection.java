package se.arkalix.net.http.consumer;

import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.net.http.client.HttpClientConnection;
import se.arkalix.security.identity.SystemIdentity;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * Connection useful for sending HTTP requests to a {@link se.arkalix consumed
 * service}.
 */
public class HttpConsumerConnection {
    private final HttpClientConnection connection;
    private final EncodingDescriptor encoding;
    private final SystemIdentity provider;
    private final String authorization;

    HttpConsumerConnection(
        final HttpClientConnection connection,
        final EncodingDescriptor encoding,
        final SystemIdentity provider,
        final String authorization)
    {
        this.connection = connection;
        this.encoding = encoding;
        this.provider = provider;
        this.authorization = authorization;
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
        Objects.requireNonNull(request, "Expected request");
        request.setEncodingIfRequired(() -> encoding.asDtoEncoding().orElseThrow(() ->
            new IllegalStateException("No DTO support is available for the " +
                "encoding \"" + encoding + "\"; the request body must be " +
                "encoded some other way")));
        if (authorization != null) {
            request.headers().setIfEmpty("authorization", authorization);
        }
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
        return send(request)
            .flatMapResult(result -> close().mapResult(ignored -> result));
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
