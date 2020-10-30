package se.arkalix.net.http;

import se.arkalix.security.SecurityDisabled;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.security.cert.Certificate;

/**
 * A connection established with a remote host, allowing for messages to either
 * be sent to or received from that host.
 */
public interface HttpConnection {
    /**
     * Gets certificate chain presented by remote host during connection
     * establishment.
     *
     * @return Remote host certificate chain.
     * @throws SecurityDisabled If this connection is not secure.
     */
    Certificate[] remoteCertificateChain();

    /**
     * Gets socket address associated with remote host.
     *
     * @return Address of host reachable via this connection.
     */
    InetSocketAddress remoteSocketAddress();

    /**
     * Gets certificate chain presented by this host during connection
     * establishment.
     *
     * @return Local host certificate chain.
     * @throws SecurityDisabled If this connection is not secure.
     */
    Certificate[] localCertificateChain();

    /**
     * Gets socket address used by this host to receive and send messages to
     * the remote host of this connection.
     *
     * @return Local network interface bound to this connection.
     */
    InetSocketAddress localSocketAddress();

    /**
     * Determines whether or not this connection is still useful for sending or
     * receiving messages to or from the remote host of this connection.
     *
     * @return {@code true} only if this connection can be used to send
     * requests to its remote host.
     */
    boolean isLive();

    /**
     * Determines whether or not this connection uses HTTPS.
     *
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
