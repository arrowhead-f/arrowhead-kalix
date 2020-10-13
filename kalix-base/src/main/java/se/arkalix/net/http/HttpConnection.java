package se.arkalix.net.http;

import se.arkalix.security.SecurityDisabled;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.security.cert.Certificate;

/**
 * A connection established with a remote peer, allowing for messages to either
 * be sent to or received from that peer.
 */
public interface HttpConnection {
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
     * Attempts to close the connection.
     *
     * @return Future completed when closing is done. Can be safely ignored.
     */
    Future<?> close();
}
