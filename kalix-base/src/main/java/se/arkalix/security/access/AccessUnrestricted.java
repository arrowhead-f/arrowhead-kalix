package se.arkalix.security.access;

import se.arkalix.description.ServiceDescription;
import se.arkalix.description.SystemDescription;
import se.arkalix.descriptor.SecurityDescriptor;

import java.util.Objects;

/**
 * Unrestricted access policy.
 * <p>
 * No certificates or other credentials are exchanged while systems
 * interact under this policy. The policy is <i>only</i> allowed for
 * services being provided by systems running in insecure mode.
 * <p>
 * Note that access policy instances of this type can be shared by multiple
 * services.
 */
public class AccessUnrestricted implements AccessPolicy {
    static AccessUnrestricted INSTANCE = new AccessUnrestricted();

    @Override
    public SecurityDescriptor descriptor() {
        return SecurityDescriptor.NOT_SECURE;
    }

    @Override
    public boolean isAuthorized(
        final SystemDescription consumer,
        final ServiceDescription service,
        final String token)
    {
        return true;
    }
}
