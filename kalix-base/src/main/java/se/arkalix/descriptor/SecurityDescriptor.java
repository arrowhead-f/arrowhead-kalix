package se.arkalix.descriptor;

import se.arkalix.security.access.AccessByCloudCertificate;
import se.arkalix.security.access.AccessByCloudWhitelist;
import se.arkalix.security.identity.SystemIdentity;

import java.util.Objects;

/**
 * Names an {@link se.arkalix.security.access.AccessPolicy access policy} that
 * Arrowhead services can be configured to use.
 */
@SuppressWarnings("unused")
public final class SecurityDescriptor {
    private final String name;

    private SecurityDescriptor(final String name) {
        this.name = Objects.requireNonNull(name, "Expected name");
    }

    /**
     * Either acquires a cached security descriptor matching the given name, or
     * creates a new descriptor.
     *
     * @param name Desired security descriptor name.
     * @return New or existing security descriptor.
     */
    public static SecurityDescriptor getOrCreate(final String name) {
        return valueOf(name);
    }

    /**
     * @return {@code true} only if this descriptor is not {@link #NOT_SECURE}.
     */
    public boolean isSecure() {
        return this != NOT_SECURE;
    }

    /**
     * Certificate {@link AccessByCloudCertificate
     * access policy}, with or without {@link
     * AccessByCloudWhitelist white-listing}.
     * <p>
     * A consuming system is trusted only if it can (1) present a certificate
     * issued by the same cloud certificate as a provider, as well as (2) the
     * {@link se.arkalix.security.identity system name} of that certificate is
     * white-listed, if required, by the service.
     */
    public static final SecurityDescriptor CERTIFICATE = new SecurityDescriptor("CERTIFICATE");

    /**
     * Unrestricted {@link se.arkalix.security.access.AccessUnrestricted access
     * policy}.
     * <p>
     * No certificates or other credentials are exchanged while systems
     * interact under this policy. The policy is <i>only</i> allowed for
     * services being provided by systems running in {@link se.arkalix.security
     * insecure mode}.
     */
    public static final SecurityDescriptor NOT_SECURE = new SecurityDescriptor("NOT_SECURE");

    /**
     * Token access policy.
     * <p>
     * A consuming system is trusted only if it can (1) present a certificate
     * issued by the same
     * {@link SystemIdentity master}
     * certificate as a provider, as well as (2) present a token originating
     * from a designated authorization system.
     */
    public static final SecurityDescriptor TOKEN = new SecurityDescriptor("TOKEN");

    /**
     * Resolves {@link SecurityDescriptor} from given {@code name}.
     *
     * @param name Name to resolve. Case insensitive.
     * @return Cached or new {@link SecurityDescriptor}.
     */
    public static SecurityDescriptor valueOf(String name) {
        name = Objects.requireNonNull(name, "Expected name").toUpperCase();
        switch (name) {
        case "CERTIFICATE": return CERTIFICATE;
        case "NOT_SECURE": return NOT_SECURE;
        case "TOKEN": return TOKEN;
        }
        return new SecurityDescriptor(name);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final var accessDescriptor = (SecurityDescriptor) other;
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
