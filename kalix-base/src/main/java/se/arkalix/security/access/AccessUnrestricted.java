package se.arkalix.security.access;

import se.arkalix.ArSystem;
import se.arkalix.ServiceRecord;
import se.arkalix.SystemRecordWithIdentity;

/**
 * Unrestricted access policy.
 * <p>
 * No certificates or other credentials are exchanged while systems
 * interact under this policy. The policy is <i>only</i> allowed for
 * services being provided by systems running in
 * {@link se.arkalix.security insecure mode}.
 * <p>
 * Note that access policy instances of this type can be shared by multiple
 * services.
 * <p>
 * Use the {@link AccessPolicy#unrestricted()} method to get an instance of
 * this class.
 */
public class AccessUnrestricted implements AccessPolicy {
    static final AccessUnrestricted instance = new AccessUnrestricted();

    @Override
    public AccessType descriptor() {
        return AccessType.NOT_SECURE;
    }

    @Override
    public boolean isAuthorized(
        final SystemRecordWithIdentity consumer,
        final ArSystem provider,
        final ServiceRecord service,
        final String token)
    {
        return true;
    }
}
