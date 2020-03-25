package se.arkalix.description;

import se.arkalix.security.identity.ArSystemCertificateChain;

import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.util.Objects;
import java.util.Optional;

/**
 * Describes an Arrowhead system, especially in terms of how it can be
 * contacted.
 */
public class SystemDescription {
    private final ArSystemCertificateChain chain;
    private final String name;
    private final InetSocketAddress remoteSocketAddress;

    /**
     * Creates new Arrowhead system description.
     *
     * @param chain               System certificate chain.
     * @param remoteSocketAddress IP-address/hostname and port through which
     *                            the system can be contacted.
     */
    public SystemDescription(final ArSystemCertificateChain chain, final InetSocketAddress remoteSocketAddress) {
        this.chain = Objects.requireNonNull(chain, "Expected certificate");
        this.remoteSocketAddress = Objects.requireNonNull(remoteSocketAddress, "Expected remoteSocketAddress");

        name = chain.systemName();
    }

    /**
     * Creates new Arrowhead system description.
     * <p>
     * This constructor is meant to be used only if the system invoking it is
     * running in insecure mode.
     *
     * @param name                System name.
     * @param remoteSocketAddress IP-address/hostname and port through which
     *                            the system can be contacted.
     */
    public SystemDescription(final String name, final InetSocketAddress remoteSocketAddress) {
        this.name = Objects.requireNonNull(name, "Expected name");
        this.remoteSocketAddress = Objects.requireNonNull(remoteSocketAddress, "Expected remoteSocketAddress");

        chain = null;
    }

    /**
     * Attempts to create new Arrowhead system description from given
     * certificate chain and remote socket address.
     * <p>
     * An empty result is returned only if any certificate in the given
     * {@code chain} is of any other type than x.509, or if fewer than 4
     * certificates are provided.
     *
     * @param chain               System certificate chain.
     * @param remoteSocketAddress IP-address/hostname and port through which
     *                            the system can be contacted.
     */
    public static Optional<SystemDescription> tryFrom(
        final Certificate[] chain,
        final InetSocketAddress remoteSocketAddress)
    {
        return ArSystemCertificateChain.tryConvert(chain)
            .map(chain0 -> new SystemDescription(chain0, remoteSocketAddress));
    }

    /**
     * @return System name.
     */
    public String name() {
        return name;
    }

    /**
     * @return The hostname/port or IP-address/port of the described system.
     */
    public InetSocketAddress remoteSocketAddress() {
        return remoteSocketAddress;
    }

    /**
     * @return System certificate.
     * @throws UnsupportedOperationException If the system does not have a
     *                                       certificate chain. This will only
     *                                       be the case if the system that
     *                                       retrieved this description runs in
     *                                       the <i>insecure</i> security mode.
     */
    public ArSystemCertificateChain certificateChain() {
        if (chain == null) {
            throw new UnsupportedOperationException("Not in secure mode");
        }
        return chain;
    }
}
