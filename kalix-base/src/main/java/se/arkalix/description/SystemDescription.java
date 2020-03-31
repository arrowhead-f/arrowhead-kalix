package se.arkalix.description;

import se.arkalix.security.identity.SystemIdentity;

import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.util.Objects;
import java.util.Optional;

/**
 * Describes an Arrowhead system, especially in terms of how it can be
 * contacted.
 */
public class SystemDescription {
    private final SystemIdentity identity;
    private final String name;
    private final InetSocketAddress remoteSocketAddress;

    /**
     * Creates new Arrowhead system description.
     *
     * @param identity            System certificate chain.
     * @param remoteSocketAddress IP-address/hostname and port through which
     *                            the system can be contacted.
     * @throws NullPointerException If {@code identity} or {@code
     *                              remoteSocketAddress} is {@code null}.
     */
    public SystemDescription(final SystemIdentity identity, final InetSocketAddress remoteSocketAddress) {
        this.identity = Objects.requireNonNull(identity, "Expected certificate");
        this.remoteSocketAddress = Objects.requireNonNull(remoteSocketAddress, "Expected remoteSocketAddress");

        name = identity.name();
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
     * @throws NullPointerException If {@code name} or {@code
     *                              remoteSocketAddress} is {@code null}.
     */
    public SystemDescription(final String name, final InetSocketAddress remoteSocketAddress) {
        this.name = Objects.requireNonNull(name, "Expected name");
        this.remoteSocketAddress = Objects.requireNonNull(remoteSocketAddress, "Expected remoteSocketAddress");

        identity = null;
    }

    /**
     * Tries to create new Arrowhead system description from given certificate
     * {@code chain} and {@code remoteSocketAddress}.
     *
     * @param chain               System certificate chain.
     * @param remoteSocketAddress IP-address/hostname and port through which
     *                            the system can be contacted.
     * @return System description, if all criteria are satisfied.
     */
    public static Optional<SystemDescription> tryFrom(
        final Certificate[] chain,
        final InetSocketAddress remoteSocketAddress)
    {
        if (remoteSocketAddress == null) {
            return Optional.empty();
        }
        return SystemIdentity.tryFrom(chain)
            .map(identity -> new SystemDescription(identity, remoteSocketAddress));
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
     * @return System identity.
     * @throws IllegalStateException If the system does not have an identity.
     *                               This will only be the case if the system
     *                               that retrieved this description runs in
     *                               the <i>insecure</i> security mode.
     */
    public SystemIdentity identity() {
        if (identity == null) {
            throw new IllegalStateException("Not in secure mode");
        }
        return identity;
    }
}
