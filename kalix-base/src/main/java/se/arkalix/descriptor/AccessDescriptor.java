package se.arkalix.descriptor;

import java.util.Objects;

/**
 * Names an access policy that Arrowhead services can use when exposing their
 * services.
 */
public final class AccessDescriptor {
    private final String name;

    private AccessDescriptor(final String name) {
        this.name = Objects.requireNonNull(name, "Expected name");
    }

    /**
     * Either acquires a cached access descriptor matching the given name, or
     * creates a new descriptor.
     *
     * @param name Desired access descriptor name.
     * @return New or existing access descriptor.
     */
    public AccessDescriptor getOrCreate(final String name) {
        return valueOf(name);
    }

    /**
     * Certificate-only access policy.
     * <p>
     * A consuming system is trusted only if it can (1) present a certificate
     * issued by the same cloud certificate as a provider, as well as (2) the
     * {@link se.arkalix.security.identity.ArSystemCertificateChain system
     * name} of that certificate is white-listed by the service.
     */
    public static final AccessDescriptor CERTIFICATE = new AccessDescriptor("CERTIFICATE");

    /**
     * Unrestricted access.
     * <p>
     * No certificates or other credentials are exchanged while systems
     * interact under this policy. The policy is <i>only</i> allowed for
     * services being provided by systems running in insecure mode.
     */
    public static final AccessDescriptor NOT_SECURE = new AccessDescriptor("NOT_SECURE");

    /**
     * Token access policy.
     * <p>
     * A consuming system is trusted only if it can (1) present a certificate
     * issued by the same
     * {@link se.arkalix.security.identity.ArSystemCertificateChain master}
     * certificate as a provider, as well as (2) present a token originating
     * from a designated authorization system.
     */
    public static final AccessDescriptor TOKEN = new AccessDescriptor("TOKEN");

    /**
     * Resolves {@link AccessDescriptor} from given {@code name}.
     *
     * @param name Name to resolve. Case insensitive.
     * @return Cached or new {@link AccessDescriptor}.
     */
    public static AccessDescriptor valueOf(String name) {
        name = Objects.requireNonNull(name, "Expected name").toUpperCase();
        switch (name) {
        case "CERTIFICATE": return CERTIFICATE;
        case "NOT_SECURE": return NOT_SECURE;
        case "TOKEN": return TOKEN;
        }
        return new AccessDescriptor(name);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final var accessDescriptor = (AccessDescriptor) other;
        return name.equals(accessDescriptor.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
