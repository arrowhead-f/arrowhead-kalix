package se.arkalix.security.access;

import se.arkalix.description.ServiceDescription;
import se.arkalix.description.SystemDescription;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.security.identity.ArSystemCertificateChain;

import java.util.Arrays;
import java.util.Objects;

/**
 * Access policy that grants access to all systems belong to the same cloud as
 * the system providing the controlled service.
 *
 * @see ArSystemCertificateChain ArCertificate
 */
public class GrantAccessIfSameCloud implements AccessByCertificate {
    private static final GrantAccessIfSameCloud INSTANCE = new GrantAccessIfSameCloud();

    private GrantAccessIfSameCloud() {}

    /**
     * @return Class instance.
     */
    public static GrantAccessIfSameCloud instance() {
        return INSTANCE;
    }

    @Override
    public SecurityDescriptor security() {
        return SecurityDescriptor.CERTIFICATE;
    }

    @Override
    public boolean isAuthorized(final SystemDescription consumer, final ServiceDescription service) {
        Objects.requireNonNull(consumer, "Expected consumer");
        Objects.requireNonNull(service, "Expected service");

        return Arrays.equals(consumer.certificateChain().cloudChain(),
            service.provider().certificateChain().cloudChain());
    }
}
