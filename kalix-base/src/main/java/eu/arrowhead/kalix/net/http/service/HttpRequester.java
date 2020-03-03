package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.net.http.HttpArrowheadSystem;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.Optional;

/**
 * Represents information about a system sending some received HTTP request.
 */
public class HttpRequester {
    /**
     * @return Certificate of request sender.
     * @throws UnsupportedOperationException If the {@link HttpArrowheadSystem}
     *                                       providing this object is not
     *                                       running in secure mode.
     */
    public X509Certificate certificate() {
        return null;
    }

    /**
     * @return The hostname/port or IP-address/port of the sender of some HTTP
     * request.
     */
    public InetSocketAddress host() {
        return null;
    }

    /**
     * @return Authorization token included in {@link HttpServiceRequest}, if
     * any.
     */
    public Optional<String> token() {
        return null;
    }

    /**
     * @return The {@code User-Agent} string of some HTTP request header, or
     * {@code null}.
     */
    public String userAgent() {
        return null;
    }
}
