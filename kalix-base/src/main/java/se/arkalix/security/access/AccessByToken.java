package se.arkalix.security.access;

import se.arkalix.ArSystem;
import se.arkalix.SystemRecordWithIdentity;
import se.arkalix.ServiceRecord;
import se.arkalix.security.access._internal.AccessToken;
import se.arkalix.util.annotation.ThreadSafe;

import java.security.PublicKey;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Token access policy.
 * <p>
 * A consuming system is granted access only if it can (1) present a
 * certificate with at least one trusted issuer, as well as (2) present a token
 * originating from a designated authorization system. The consuming system
 * does not have to be a member of the same local cloud as the system using
 * this access policy.
 * <p>
 * The designated authorization system is identified by a {@link PublicKey},
 * which can either be specified directly or be resolved at a later time.
 * Authorization system public key resolution could, for example, be performed
 * by some suitable {@link se.arkalix.plugin.Plugin plugin}. The authorization
 * system key is set or replaced using the {@link #authorizationKey(PublicKey)}
 * method.
 * <p>
 * Note that access policy instances of this type can be shared by multiple
 * services.
 */
public class AccessByToken implements AccessPolicy {
    private final AtomicReference<PublicKey> authorizationKey;

    /**
     * Creates new certificate and token access policy without an authorization
     * system public key.
     * <p>
     * The key must be set later via the {@link #authorizationKey(PublicKey)}
     * method, perhaps via a {@link se.arkalix.plugin.Plugin plugin}. This
     * access policy will prevent all access until a valid key has been set.
     */
    public AccessByToken() {
        authorizationKey = new AtomicReference<>(null);
    }

    /**
     * Creates new certificate and token access policy with the given
     * authorization system public key.
     * <p>
     * The key can be changed later via the
     * {@link #authorizationKey(PublicKey)} method.
     *
     * @param authorizationKey Public key of issuer of acceptable access tokens.
     */
    public AccessByToken(final PublicKey authorizationKey) {
        this.authorizationKey = new AtomicReference<>(authorizationKey);
    }

    /**
     * Sets authorization system public key.
     * <p>
     * The key is used to determine if provided access tokens were created by
     * the system owning the corresponding private key, which in most scenarios
     * can be expected to be an authorization system belonging to the same
     * cloud as the service provider using this access policy.
     *
     * @param authorizationKey Public key of designated authorization system.
     */
    @ThreadSafe
    public void authorizationKey(final PublicKey authorizationKey) {
        this.authorizationKey.set(authorizationKey);
    }

    @Override
    public AccessType descriptor() {
        return AccessType.TOKEN;
    }

    @Override
    public boolean isAuthorized(
        final SystemRecordWithIdentity consumer,
        final ArSystem provider,
        final ServiceRecord service,
        final String token)
        throws AccessTokenException
    {
        Objects.requireNonNull(consumer, "consumer");
        Objects.requireNonNull(service, "service");

        if (token == null) {
            return false;
        }

        final var senderKey = authorizationKey.get();
        if (senderKey == null) {
            throw new IllegalStateException("Cannot verify token; no authorization key is available");
        }

        final var receiverKey = provider.identity().privateKey();
        final var token0 = AccessToken.read(token, receiverKey, senderKey);

        final var cid = token0.cid();
        final var cn = consumer.identity().commonName();
        return cn.startsWith(cid) && cn.charAt(cid.length()) == '.' &&
            Objects.equals(token0.sid(), service.name()) &&
            service.interfaces().contains(token0.iid());
    }
}
