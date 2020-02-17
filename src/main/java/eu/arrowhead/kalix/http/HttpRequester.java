package eu.arrowhead.kalix.http;

import java.net.InetSocketAddress;
import java.security.cert.Certificate;

/**
 * Represents information about a system sending some received HTTP request.
 */
public class HttpRequester {
    /**
     * @return Certificate of request sender, if available.
     */
    public Certificate certificate() {
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
     * @return The {@code User-Agent} string of some HTTP request header, or
     * {@code null}.
     */
    public String userAgent() {
        return null;
    }
}
