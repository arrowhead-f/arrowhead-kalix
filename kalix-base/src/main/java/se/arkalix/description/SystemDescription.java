package se.arkalix.description;

import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.Objects;

/**
 * Describes an Arrowhead system, especially in terms of how it can be
 * contacted.
 */
public class SystemDescription {
    private final String name;
    private final InetSocketAddress remoteSocketAddress;
    private final PublicKey publicKey;

    /**
     * Creates new Arrowhead system description.
     *
     * @param name                System name.
     * @param remoteSocketAddress IP-address/hostname and port through which
     *                            the system can be contacted.
     * @param publicKey           System public key.
     */
    public SystemDescription(
        final String name,
        final InetSocketAddress remoteSocketAddress,
        final PublicKey publicKey)
    {
        this.name = Objects.requireNonNull(name, "Expected name");
        this.remoteSocketAddress = Objects.requireNonNull(remoteSocketAddress, "Expected remoteSocketAddress");
        this.publicKey = publicKey;
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
     * @return System public key.
     * @throws UnsupportedOperationException If the system does not have a
     *                                       certificate chain. This will only
     *                                       be the case if the system that
     *                                       retrieved this description runs in
     *                                       the <i>insecure</i> security mode.
     */
    public PublicKey publicKey() {
        if (publicKey == null) {
            throw new UnsupportedOperationException("Not in secure mode");
        }
        return publicKey;
    }
}
