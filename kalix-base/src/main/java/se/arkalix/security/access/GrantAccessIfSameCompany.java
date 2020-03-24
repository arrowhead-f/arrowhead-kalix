package se.arkalix.security.access;

import se.arkalix.description.ServiceDescription;
import se.arkalix.description.SystemDescription;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.security.identity.ArSystemCertificateChain;

import java.util.Arrays;
import java.util.Objects;

/**
 * Access policy that grants access to all systems belong to the same company
 * as the the system providing the controlled service.
 *
 * @see ArSystemCertificateChain ArCertificate
 */
public class GrantAccessIfSameCompany implements AccessByCertificate {
    private static final GrantAccessIfSameCompany INSTANCE = new GrantAccessIfSameCompany();

    private GrantAccessIfSameCompany() {}

    /**
     * @return Class instance.
     */
    public static GrantAccessIfSameCompany instance() {
        return INSTANCE;
    }

    @Override
    public SecurityDescriptor security() {
        return SecurityDescriptor.CERTIFICATE;
    }

    @Override
    public boolean isAuthorized(final SystemDescription consumer, final ServiceDescription service) {
        Objects.requireNonNull(consumer, "Expected system");
        Objects.requireNonNull(service, "Expected service");

        return Arrays.equals(consumer.certificateChain().companyChain(),
            service.provider().certificateChain().companyChain());
    }
}
