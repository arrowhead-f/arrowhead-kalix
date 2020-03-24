package se.arkalix.security.access;

import se.arkalix.descriptor.SecurityDescriptor;

/**
 * Access policy that grants unrestricted access to all systems.
 * <p>
 * If a system running in secure mode uses this policy for one of its services,
 * any system issued by any {@link se.arkalix.security.identity.ArTrustStore
 * trust store} certificate is granted access. It should be noted, however,
 * that the current version of Arrowhead Framework does not permit the use of
 * this access policy for systems running secure mode. If insecure mode is
 * used, access is granted to all systems.
 * <p>
 * Use of this access policy is <i>not recommended</i> for most kinds of
 * deployment scenarios. It is, however, the only access policy that can be
 * used by systems running in insecure mode.
 */
public class GrantAccessToEveryone implements AccessPolicy {
    private static final GrantAccessToEveryone INSTANCE = new GrantAccessToEveryone();

    private GrantAccessToEveryone() {}

    /**
     * @return Class instance.
     */
    public static GrantAccessToEveryone instance() {
        return INSTANCE;
    }

    @Override
    public SecurityDescriptor security() {
        return SecurityDescriptor.NOT_SECURE;
    }
}
