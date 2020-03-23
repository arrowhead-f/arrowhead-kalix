package se.arkalix.security.access;

import se.arkalix.description.ServiceDescription;
import se.arkalix.description.SystemDescription;
import se.arkalix.descriptor.AccessDescriptor;

/**
 * Access policy that grants access to all systems belong to the same cloud as
 * the system providing the controlled service, as well as having an access
 * token, provided by the cloud <i>Authorization</i> system, that is valid for
 * the service.
 *
 * @see se.arkalix.security.identity.ArCertificate ArCertificate
 */
public class AccessSameCloudToken implements AccessPolicy {
    @Override
    public AccessDescriptor descriptor() {
        return AccessDescriptor.TOKEN;
    }

    @Override
    public boolean isAuthorized(final SystemDescription system, final ServiceDescription service, final byte[] token) {
        if (token == null || token.length < 3) {
            return false;
        }
        // TODO: Verify token.
        return AccessSameCloud.instance().isAuthorized(system, service, token);
    }

}
