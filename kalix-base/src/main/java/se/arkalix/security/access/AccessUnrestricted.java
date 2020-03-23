package se.arkalix.security.access;

import se.arkalix.description.ServiceDescription;
import se.arkalix.description.SystemDescription;
import se.arkalix.descriptor.AccessDescriptor;

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
public class AccessUnrestricted implements AccessPolicy {
    private static final AccessUnrestricted INSTANCE = new AccessUnrestricted();

    private AccessUnrestricted() {}

    /**
     * @return Class instance.
     */
    public static AccessUnrestricted instance() {
        return INSTANCE;
    }

    @Override
    public AccessDescriptor descriptor() {
        return AccessDescriptor.UNRESTRICTED;
    }

    @Override
    public boolean isAuthorized(final SystemDescription system, final ServiceDescription service, final byte[] token) {
        return true;
    }


}
