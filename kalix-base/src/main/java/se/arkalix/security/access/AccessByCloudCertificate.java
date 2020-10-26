package se.arkalix.security.access;

import se.arkalix.ArSystem;
import se.arkalix.ServiceRecord;
import se.arkalix.SystemRecordWithIdentity;
import se.arkalix.descriptor.SecurityDescriptor;

import java.util.*;

/**
 * Same-cloud access policy.
 * <p>
 * A consuming system is granted access only if it can present a certificate
 * issued by the same local cloud certificate as the system using this policy.
 * <p>
 * Use the {@link AccessPolicy#cloud()} method to get an instance of this
 * class.
 */
public class AccessByCloudCertificate implements AccessPolicy {
    static final AccessByCloudCertificate instance = new AccessByCloudCertificate();

    private AccessByCloudCertificate() {}

    @Override
    public SecurityDescriptor descriptor() {
        return SecurityDescriptor.CERTIFICATE;
    }

    @Override
    public boolean isAuthorized(
        final SystemRecordWithIdentity consumer,
        final ArSystem provider,
        final ServiceRecord service,
        final String token)
    {
        Objects.requireNonNull(consumer, "Expected consumer");
        Objects.requireNonNull(service, "Expected service");

        return Objects.equals(consumer.identity().cloud(), provider.identity().cloud());
    }
}
