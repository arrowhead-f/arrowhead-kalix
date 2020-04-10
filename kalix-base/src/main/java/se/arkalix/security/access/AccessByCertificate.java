package se.arkalix.security.access;

import se.arkalix.ArSystem;
import se.arkalix.description.ServiceDescription;
import se.arkalix.description.ConsumerDescription;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.security.identity.SystemIdentity;
import se.arkalix.util.annotation.ThreadSafe;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Certificate-only same-cloud access policy.
 * <p>
 * A consuming system is granted access only if it can present a certificate
 * issued by the same local cloud certificate as the system using this policy.
 * <p>
 * Use the {@link AccessPolicy#cloud()} method to get an instance of this
 * class.
 */
public class AccessByCertificate implements AccessPolicy {
    static final AccessByCertificate INSTANCE = new AccessByCertificate();

    private AccessByCertificate() {}

    @Override
    public SecurityDescriptor descriptor() {
        return SecurityDescriptor.CERTIFICATE;
    }

    @Override
    public boolean isAuthorized(
        final ConsumerDescription consumer,
        final ArSystem provider,
        final ServiceDescription service,
        final String token)
    {
        Objects.requireNonNull(consumer, "Expected consumer");
        Objects.requireNonNull(service, "Expected service");

        return Objects.equals(consumer.identity().cloud(), provider.identity().cloud());
    }
}
