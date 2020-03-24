package se.arkalix.security.access;

import se.arkalix.description.ServiceDescription;
import se.arkalix.description.SystemDescription;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.security.identity.ArSystemCertificateChain;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Access policy that grants access to all systems belong to the same cloud as
 * the system providing the controlled service, as well as having their names
 * in a unmodifiable white-list.
 *
 * @see ArSystemCertificateChain ArCertificate
 */
public class GrantAccessIfSameCloudAndWhitelisted implements AccessByCertificate {
    private final Set<String> whitelist;

    /**
     * Creates new access policy that grants access to systems from the same
     * cloud as the access controller, if their system names appear in the
     * given {@code whitelist.
     *
     * @param whitelist White-listed systems.
     */
    public GrantAccessIfSameCloudAndWhitelisted(final String... whitelist) {
        this(List.of(whitelist));
    }

    /**
     * Creates new access policy that grants access to systems from the same
     * cloud as the access controller, if their system names appear in the
     * given {@code whitelist.
     *
     * @param whitelist White-listed systems.
     */
    public GrantAccessIfSameCloudAndWhitelisted(final Collection<String> whitelist) {
        this.whitelist = whitelist.stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public SecurityDescriptor security() {
        return SecurityDescriptor.CERTIFICATE;
    }

    @Override
    public boolean isAuthorized(final SystemDescription consumer, final ServiceDescription service) {
        Objects.requireNonNull(consumer, "Expected system");

        if (!whitelist.contains(consumer.name())) {
            return false;
        }
        return GrantAccessIfSameCloud.instance().isAuthorized(consumer, service);
    }
}
