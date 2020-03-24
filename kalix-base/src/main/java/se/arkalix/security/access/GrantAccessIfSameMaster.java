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
public class GrantAccessIfSameMaster implements AccessByCertificate {
    private static final GrantAccessIfSameMaster INSTANCE = new GrantAccessIfSameMaster();

    private GrantAccessIfSameMaster() {}

    /**
     * @return Class instance.
     */
    public static GrantAccessIfSameMaster instance() {
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

        return Arrays.equals(consumer.certificateChain().masterChain(),
            service.provider().certificateChain().masterChain());
    }
}
