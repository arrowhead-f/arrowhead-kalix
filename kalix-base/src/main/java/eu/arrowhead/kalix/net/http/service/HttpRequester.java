package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.net.http.HttpArrowheadSystem;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents information about a system sending some received HTTP request.
 */
public class HttpRequester {
    private final X509Certificate certificate;
    private final InetSocketAddress remoteSocketAddress;
    private final String token;

    /**
     * Creates new object containing details about the sender of some incoming
     * HTTP request.
     *
     * @param certificate         Certificate of sender, if running in secure
     *                            mode.
     * @param remoteSocketAddress Socket address of sender.
     * @param token               Authorization token of sender, if any.
     */
    public HttpRequester(
        final X509Certificate certificate,
        final InetSocketAddress remoteSocketAddress,
        final String token)
    {
        this.certificate = certificate;
        this.remoteSocketAddress = Objects.requireNonNull(remoteSocketAddress, "Expected remoteSocketAddress");
        this.token = token;
    }

    /**
     * @return Certificate of request sender.
     * @throws UnsupportedOperationException If the {@link HttpArrowheadSystem}
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
     * request.
     */
    public InetSocketAddress remoteSocketAddress() {
        return remoteSocketAddress;
    }

    /**
     * @return Authorization token included in {@code HttpServiceRequest}, if
     * any.
     */
    public Optional<String> token() {
        return Optional.ofNullable(token);
    }
}
