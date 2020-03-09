package eu.arrowhead.kalix.net.http;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;

/**
 * Represents information about a remote system that is being or has been
 * communicated with.
 */
public interface HttpPeer {
    /**
     * @return Certificate of peer.
     * @throws UnsupportedOperationException If the connection to the peer is
     *                                       not encrypted in such a way that a
     *                                       peer certificate becomes available.
     */
    default X509Certificate certificate() {
        return certificateChain()[0];
    }

    /**
     * @return Certificate chain of the peer system. The certificate at index 0
     * is the one owned by the peer, and the other ones are its issuers.
     * @throws UnsupportedOperationException If the connection to the peer is
     *                                       not encrypted in such a way that a
     *                                       peer certificate becomes available.
     */
    X509Certificate[] certificateChain();

    /**
     * @return The hostname/port or IP-address/port of the peer.
     */
    InetSocketAddress remoteSocketAddress();
}
