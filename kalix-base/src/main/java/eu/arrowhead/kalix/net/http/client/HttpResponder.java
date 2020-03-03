package eu.arrowhead.kalix.net.http.client;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.Objects;

/**
 * Represents information about a system sending some received HTTP response.
 */
public class HttpResponder {
    private final X509Certificate certificate;
    private final InetSocketAddress remoteSocketAddress;

    /**
     * Creates new object containing details about the sender of some incoming
     * HTTP response.
     *
     * @param certificate         Certificate of sender, if running in secure
     *                            mode.
     * @param remoteSocketAddress Socket address of sender.
     */
    public HttpResponder(final X509Certificate certificate, final InetSocketAddress remoteSocketAddress) {
        this.certificate = certificate;
        this.remoteSocketAddress = Objects.requireNonNull(remoteSocketAddress, "Expected remoteSocketAddress");
    }

    /**
     * @return Certificate of response sender.
     * @throws UnsupportedOperationException If the {@link HttpClient}
     *                                       providing this object is not
     *                                       running in secure mode.
     */
    public X509Certificate certificate() {
        if (certificate == null) {
            throw new UnsupportedOperationException("Not in secure mode");
        }
        return certificate;
    }

    /**
     * @return The hostname/port or IP-address/port of the sender of some HTTP
     * response.
     */
    public InetSocketAddress remoteSocketAddress() {
        return remoteSocketAddress;
    }
}
