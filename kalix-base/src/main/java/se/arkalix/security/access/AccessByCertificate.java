package se.arkalix.security.access;

import se.arkalix.description.ServiceDescription;
import se.arkalix.description.SystemDescription;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.security.identity.SystemIdentity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Certificate-only access policy.
 * <p>
 * A consuming system is trusted only if it can (1) present a certificate
 * issued by the same cloud certificate as a provider, as well as (2) the
 * {@link SystemIdentity system name} of that certificate is white-listed by
 * the service.
 * <p>
 * Note that access policy instances of this type can be shared by multiple
 * services.
 */
public class AccessByCertificate implements AccessPolicy {
    static final AccessByCertificate INSTANCE = new AccessByCertificate(null);

    private final Set<String> whitelist;

    /**
     * Creates new certificate-only access policy. If a white-list is provided,
     * only systems with the provided system names will be granted access.
     * <p>
     * Note that the white-listed names are not full names. Only the system
     * name parts, as described {@link SystemIdentity here}.
     *
     * @param whitelist Collection of names of systems to be allowed access.
     */
    public AccessByCertificate(final Collection<String> whitelist) {
        this.whitelist = whitelist != null
            ? whitelist.stream().collect(Collectors.toUnmodifiableSet())
            : null;
    }

    @Override
    public SecurityDescriptor descriptor() {
        return SecurityDescriptor.CERTIFICATE;
    }

    @Override
    public boolean isAuthorized(
        final SystemDescription consumer,
        final ServiceDescription service,
        final String token)
    {
        Objects.requireNonNull(consumer, "Expected consumer");
        Objects.requireNonNull(service, "Expected service");

        final var consumerCloud = consumer.identity().issuer();
        if (consumerCloud.isEmpty()) {
            return false;
        }

        final var providerCloud = service.provider().identity().issuer();
        if (providerCloud.isEmpty()) {
            throw new IllegalStateException("Providing system identity not " +
                "issued by a cloud certificate");
        }

        return Objects.equals(consumerCloud.get(), providerCloud.get()) &&
            (whitelist == null || whitelist.contains(consumer.name()));
    }
}
