package se.arkalix.net.http;

import se.arkalix.security.SecurityDisabled;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.security.cert.Certificate;

/**
 * An HTTP connection through which this application can send requests to a
 * remote host.
 *
 * @param <Request>  Type of HTTP request.
 * @param <Response> Type of HTTP response.
 */
@SuppressWarnings("unused")
public interface HttpOutgoingConnection<Request extends HttpOutgoingRequest<?>, Response extends HttpIncomingResponse> {
    /**
     * @return Certificate chain associated with host reachable via this
     * connection.
     * @throws SecurityDisabled If this connection is not secure.
     */
    Certificate[] remoteCertificateChain();

    /**
     * @return Address of host reachable via this connection.
     */
    InetSocketAddress remoteSocketAddress();

    /**
     * @return Certificate chain used by this host to establish this
     * connection.
     * @throws SecurityDisabled If this connection is not secure.
     */
    Certificate[] localCertificateChain();

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
     * @return {@code true} only if this is an HTTPS connection.
     */
    boolean isSecure();

    /**
     * Sends given {@code request} to connected remote host, awaits either a
     * response or an error and then completes the returned {@code Future} with
     * the resulting response or error.
     *
     * @param request HTTP request to send.
     * @return Future of {@code Response}.
     */
    Future<Response> send(final Request request);

    /**
     * Sends given {@code request} to connected remote host, awaits either a
     * response or an error, <i>closes this connection</i> and then completes
     * the returned {@code Future} with the resulting response or error.
     *
     * @param request HTTP request to send.
     * @return Future of {@code Response}.
     */
    Future<Response> sendAndClose(final Request request);

    /**
     * Attempts to close the connection.
     *
     * @return Future completed when closing is done. Can be safely ignored.
     */
    Future<?> close();
}
