package se.arkalix.security.access;

import se.arkalix.description.ServiceDescription;
import se.arkalix.description.SystemDescription;
import se.arkalix.descriptor.AccessDescriptor;

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
 * @see se.arkalix.security.identity.ArCertificate ArCertificate
 */
public class AccessSameCloudWhitelist implements AccessPolicy {
    private final Set<String> whitelist;

    /**
     * Creates new access policy that grants access to systems from the same
     * cloud as the access controller, if their system names appear in the
     * given {@code whitelist.
     *
     * @param whitelist White-listed systems.
     */
    public AccessSameCloudWhitelist(final String... whitelist) {
        this(List.of(whitelist));
    }

    /**
     * Creates new access policy that grants access to systems from the same
     * cloud as the access controller, if their system names appear in the
     * given {@code whitelist.
     *
     * @param whitelist White-listed systems.
     */
    public AccessSameCloudWhitelist(final Collection<String> whitelist) {
        this.whitelist = whitelist.stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public AccessDescriptor descriptor() {
        return AccessDescriptor.CERTIFICATE;
    }

    @Override
    public boolean isAuthorized(final SystemDescription system, final ServiceDescription service, final byte[] token) {
        Objects.requireNonNull(system, "Expected system");

        if (!whitelist.contains(system.name())) {
            return false;
        }
        return AccessSameCloud.instance().isAuthorized(system, service, token);
    }
}
