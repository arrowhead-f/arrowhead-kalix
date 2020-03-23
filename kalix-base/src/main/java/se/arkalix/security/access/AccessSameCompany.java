package se.arkalix.security.access;

import se.arkalix.description.ServiceDescription;
import se.arkalix.description.SystemDescription;
import se.arkalix.descriptor.AccessDescriptor;

import java.util.Objects;

/**
 * Access policy that grants access to all systems belong to the same company
 * as the the system providing the controlled service.
 *
 * @see se.arkalix.security.identity.ArCertificate ArCertificate
 */
public class AccessSameCompany implements AccessPolicy {
    private static final AccessSameCompany INSTANCE = new AccessSameCompany();

    private AccessSameCompany () {}

    /**
     * @return Class instance.
     */
    public static AccessSameCompany instance() {
        return INSTANCE;
    }

    @Override
    public AccessDescriptor descriptor() {
        return AccessDescriptor.CERTIFICATE;
    }

    @Override
    public boolean isAuthorized(final SystemDescription system, final ServiceDescription service, final byte[] token) {
        Objects.requireNonNull(system, "Expected system");
        Objects.requireNonNull(service, "Expected service");

        final var consumerCloud = system.certificate().issuer().orElse(null);
        if (consumerCloud == null) {
            return false;
        }
        final var consumerCompany = consumerCloud.issuer().orElse(null);
        if (consumerCompany == null) {
            return false;
        }

        final var provider = service.provider();
        final var providerCompany = provider.certificate()
            .issuer()
            .orElseThrow(() -> new IllegalStateException("Providing system " +
                "\"" + provider.name() + "\" is not associated with a cloud " +
                "certificate; secure mode required"))
            .issuer()
            .orElseThrow(() -> new IllegalStateException("Providing system " +
                "\"" + provider.name() + "\" is not associated with a " +
                "company certificate"));

        return consumerCompany.equals(providerCompany);
    }
}
