package se.arkalix.security.access;

import se.arkalix.description.ServiceDescription;
import se.arkalix.description.SystemDescription;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.internal.security.access.ArToken;
import se.arkalix.security.identity.ArSystemCertificateChain;
import se.arkalix.security.identity.ArSystemKeyStore;

import java.security.PublicKey;
import java.util.Objects;

/**
 * Access policy that grants access to all systems belong to the same cloud as
 * the system providing the controlled service, as well as having an access
 * token, provided by the cloud <i>Authorization</i> system, that is valid for
 * the service.
 *
 * @see ArSystemCertificateChain ArCertificate
 */
public class GrantAccessIfSameCloudAndHasToken implements AccessByToken {
    private final PublicKey authorizationKey;

    public GrantAccessIfSameCloudAndHasToken(final PublicKey authorizationKey) {
        this.authorizationKey = authorizationKey;
    }

    @Override
    public SecurityDescriptor security() {
        return SecurityDescriptor.TOKEN;
    }

    @Override
    public boolean isAuthorized(
        final SystemDescription consumer,
        final ServiceDescription service,
        final byte[] token) throws Exception
    {
        final var receiverKey = ((ArSystemKeyStore) service.provider().certificateChain()).privateKey();
        final var token0 = ArToken.read(token, receiverKey, authorizationKey);

        final var cid = token0.cid();
        final var cn = consumer.certificateChain().systemCommonName();
        if (!cn.startsWith(cid) || cn.charAt(cid.length()) != '.') {
            return false;
        }

        if (!Objects.equals(token0.sid(), service.name())) {
            return false;
        }

        if (!service.supportedInterfaces().contains(token0.iid())) {
            return false;
        }

        return GrantAccessIfSameCloud.instance().isAuthorized(consumer, service);
    }
}
