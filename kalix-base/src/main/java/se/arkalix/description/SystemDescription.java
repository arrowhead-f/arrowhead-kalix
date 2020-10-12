package se.arkalix.description;

import se.arkalix.internal.description.DefaultSystemDescription;
import se.arkalix.security.SecurityDisabled;
import se.arkalix.security.identity.SystemIdentity;

import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.Objects;

/**
 * Describes an Arrowhead system that can, potentially, be communicated with.
 */
public interface SystemDescription {
    /**
     * Creates new Arrowhead provider system description from given system
     * identity and remote socket address.
     *
     * @param identity            Identity of described provider system.
     * @param remoteSocketAddress Remote socket address of the same system.
     * @return New provider description.
     */
    static SystemDescription from(final SystemIdentity identity, final InetSocketAddress remoteSocketAddress) {
        Objects.requireNonNull(identity);
        return new DefaultSystemDescription(identity.name(), identity.publicKey(), remoteSocketAddress);
    }

    /**
     * Creates new Arrowhead provider system description.
     *
     * @param name                System name.
     * @param publicKey           System public key.
     * @param remoteSocketAddress IP-address/hostname and port through which
     *                            the system can be contacted.
     * @throws NullPointerException If {@code name} or {@code
     *                              remoteSocketAddress} is {@code null}.
     */
    static SystemDescription from(
        final String name,
        final PublicKey publicKey,
        final InetSocketAddress remoteSocketAddress
    ) {
        return new DefaultSystemDescription(name, publicKey, remoteSocketAddress);
    }

    /**
     * Creates new Arrowhead provider system description.
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
    static SystemDescription from(final String name, final InetSocketAddress remoteSocketAddress) {
        return new DefaultSystemDescription(name, null, remoteSocketAddress);
    }

    /**
     * Gets name of described system.
     *
     * @return System name.
     */
    String name();

    /**
     * The socket address of the described system.
     *
     * @return The hostname/port or IP-address/port of the described system.
     */
    InetSocketAddress socketAddress();

    /**
     * Determines whether the described service is known to be running in
     * secure mode, in which cause {@link #publicKey()} ()} is guaranteed not
     * to
     * throw when called.
     *
     * @return {@code true} only if the described service is known to be
     * running in secure mode.
     */
    boolean isSecure();

    /**
     * Gets public key of peer system, or throws if not in secure mode.
     *
     * @return System public key.
     * @throws SecurityDisabled If the system does not have an public key.
     *                          This will only be the case if the described
     *                          system runs in the <i>insecure</i> {@link
     *                          se.arkalix.security security mode}, which it
     *                          only can do if the system that requested it
     *                          also runs in insecure mode.
     */
    PublicKey publicKey();
}
