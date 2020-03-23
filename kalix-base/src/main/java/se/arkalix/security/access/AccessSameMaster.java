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
public class AccessSameMaster implements AccessPolicy {
    private static final AccessSameMaster INSTANCE = new AccessSameMaster();

    private AccessSameMaster() {}

    /**
     * @return Class instance.
     */
    public static AccessSameMaster instance() {
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
        final var consumerMaster = consumerCompany.issuer().orElse(null);
        if (consumerMaster == null) {
            return false;
        }

        final var provider = service.provider();
        final var providerMaster = provider.certificate()
            .issuer()
            .orElseThrow(() -> new IllegalStateException("Providing system " +
                "\"" + provider.name() + "\" is not associated with a cloud " +
                "certificate; secure mode required"))
            .issuer()
            .orElseThrow(() -> new IllegalStateException("Providing system " +
                "\"" + provider.name() + "\" is not associated with a " +
                "company certificate"))
            .issuer()
            .orElseThrow(() -> new IllegalStateException("Providing system " +
                "\"" + provider.name() + "\" is not associated with a " +
                "master certificate"));

        return consumerMaster.equals(providerMaster);
    }
}
