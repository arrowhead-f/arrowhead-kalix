package eu.arrowhead.kalix.http.client;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.Optional;

/**
 * Represents information about a system sending some received HTTP response.
 */
public class HttpResponder {
    /**
     * @return Certificate of response sender, if available.
     */
    public Optional<X509Certificate> certificate() {
        return null;
    }

    /**
     * @return The hostname/port or IP-address/port of the sender of some HTTP
     * response.
     */
    public InetSocketAddress host() {
        return null;
    }
}
