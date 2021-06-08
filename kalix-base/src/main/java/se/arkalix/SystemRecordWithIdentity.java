package se.arkalix;

import se.arkalix._internal.DefaultSystemRecordWithIdentity;
import se.arkalix.security.SecurityDisabled;
import se.arkalix.security.identity.SystemIdentity;

import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Describes an Arrowhead system with a known {@link #identity() identity}, if
 * the described system is running in {@link se.arkalix.security secure mode}.
 */
public interface SystemRecordWithIdentity extends SystemRecord {
    /**
     * Creates new Arrowhead consumer system description.
     *
     * @param identity            System certificate chain.
     * @param remoteSocketAddress IP-address/hostname and port through which
     *                            the system can be contacted.
     * @return Description of consumer system.
     * @throws NullPointerException If {@code identity} or {@code
     *                              remoteSocketAddress} is {@code null}.
     */
    static SystemRecordWithIdentity from(final SystemIdentity identity, final InetSocketAddress remoteSocketAddress) {
        Objects.requireNonNull(identity, "identity");
        return new DefaultSystemRecordWithIdentity(identity.name(), identity, remoteSocketAddress, null);
    }

    /**
     * Creates new Arrowhead consumer system description.
     * <p>
     * This constructor is meant to be used only if the system invoking it is
     * running in insecure mode.
     *
     * @param name                System name.
     * @param remoteSocketAddress IP-address/hostname and port through which
     *                            the system can be contacted.
     * @param metadata            System metadata.
     * @return Description of consumer system.
     * @throws NullPointerException If {@code name} or {@code
     *                              remoteSocketAddress} is {@code null}.
     */
    static SystemRecordWithIdentity from(
        final String name,
        final InetSocketAddress remoteSocketAddress,
        Map<String, String> metadata
    ) {
        return new DefaultSystemRecordWithIdentity(name, null, remoteSocketAddress, metadata);
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
    static Optional<SystemRecordWithIdentity> tryFrom(
        final Certificate[] chain,
        final InetSocketAddress remoteSocketAddress
    ) {
        if (remoteSocketAddress == null) {
            return Optional.empty();
        }
        return SystemIdentity.tryFrom(chain)
            .map(identity -> from(identity, remoteSocketAddress));
    }

    @Override
    default PublicKey publicKey() {
        return identity().publicKey();
    }

    /**
     * Gets identity of peer system, or throws if not in secure mode.
     *
     * @return System identity.
     * @throws SecurityDisabled If the system does not have an identity. This
     *                          will only be the case if the system that
     *                          retrieved this description runs in the
     *                          <i>insecure</i> {@link se.arkalix.security
     *                          security mode}.
     */
    SystemIdentity identity();
}
