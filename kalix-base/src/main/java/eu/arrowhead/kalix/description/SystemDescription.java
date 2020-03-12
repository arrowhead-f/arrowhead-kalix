package eu.arrowhead.kalix.description;

import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;

/**
 * Describes an Arrowhead system, especially in terms of how it can be
 * contacted.
 */
public class SystemDescription {
    private final String name;
    private final InetSocketAddress remoteSocketAddress;
    private final X509Certificate[] certificateChain;

    /**
     * Creates new Arrowhead system description.
     *
     * @param name                System name.
     * @param remoteSocketAddress IP-address/hostname and port through which
     *                            the system can be contacted.
     * @param certificateChain    System certificate chain.
     */
    public SystemDescription(
        final String name,
        final InetSocketAddress remoteSocketAddress,
        final X509Certificate[] certificateChain)
    {
        this.name = Objects.requireNonNull(name, "Expected name");
        this.remoteSocketAddress = Objects.requireNonNull(remoteSocketAddress, "Expected remoteSocketAddress");
        this.certificateChain = certificateChain != null && certificateChain.length == 0
            ? certificateChain
            : null;
    }

    /**
     * @return System name.
     */
    public String name() {
        return name;
    }

    /**
     * @return The hostname/port or IP-address/port of the peer.
     */
    public InetSocketAddress remoteSocketAddress() {
        return remoteSocketAddress;
    }

    /**
     * @return Certificate chain of system, if any.
     */
    public X509Certificate[] certificateChain() {
        return certificateChain;
    }

    /**
     * @return System certificate, if any.
     */
    public X509Certificate certificate() {
        return certificateChain[0];
    }

    /**
     * @return Public key of system, if any.
     */
    public PublicKey publicKey() {
        return certificate().getPublicKey();
    }
}
