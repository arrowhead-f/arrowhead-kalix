package eu.arrowhead.kalix.net.http.client;

import eu.arrowhead.kalix.net.http.HttpArrowheadSystem;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;

/**
 * Represents information about a system sending some received HTTP response.
 */
public interface HttpResponder {
    /**
     * @return Certificate of request sender.
     * @throws UnsupportedOperationException If the {@link HttpArrowheadSystem}
     *                                       providing this object is not
     *                                       running in secure mode.
     */
    default X509Certificate certificate() {
        return certificateChain()[0];
    }

    /**
     * @return Certificate chain of the request sender. The certificate at
     * index 0 is the one owned by the request sender, and the other ones are
     * its issuers.
     * @throws UnsupportedOperationException If the {@link HttpArrowheadSystem}
     *                                       providing this object is not
     *                                       running in secure mode.
     */
    X509Certificate[] certificateChain();

    /**
     * @return The hostname/port or IP-address/port of the request sender.
     */
    InetSocketAddress remoteSocketAddress();
}
