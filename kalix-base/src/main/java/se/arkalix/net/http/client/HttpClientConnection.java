package se.arkalix.net.http.client;

import se.arkalix.description.SystemDescription;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.util.Optional;

/**
 * Connection useful for sending HTTP requests to a single remote socket
 * address.
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
     * @throws IllegalStateException If the client is not running in secure
     *                               mode.
     */
    Certificate[] certificateChain();

    /**
     * @return Description of host reachable via this connection as an
     * Arrowhead system, or nothing if the client is not running in secure
     * mode or if the certificate chain of the remote host is not superficially
     * Arrowhead compliant.
     */
    default SystemDescription describe() {
        try {
            return SystemDescription.tryFrom(certificateChain(), remoteSocketAddress())
                .orElseThrow(() -> new IllegalStateException("Bad certificate chain"));
        }
        catch (final IllegalArgumentException exception) {
            throw new IllegalStateException("Bad certificate chain", exception);
        }
    }

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
