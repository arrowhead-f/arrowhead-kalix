package eu.arrowhead.kalix.http;

import java.net.InetSocketAddress;

/**
 * Represents information about the sender of some HTTP request.
 */
public class HttpRequester {
    /**
     * @return The {@code User-Agent} string of some HTTP request header, or
     * {@code null}.
     */
    public String getUserAgent() {
        return null;
    }

    /**
     * @return The hostname/port or IP-address/port of the sender of some HTTP
     * request.
     */
    public InetSocketAddress getHost() {
        return null;
    }
}
