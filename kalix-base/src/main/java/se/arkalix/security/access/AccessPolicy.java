package se.arkalix.security.access;

import se.arkalix.ArSystem;
import se.arkalix.ServiceRecord;
import se.arkalix.SystemRecordWithIdentity;
import se.arkalix.security.identity.SystemIdentity;
import se.arkalix.util.annotation.ThreadSafe;

import java.security.PublicKey;
import java.util.Collection;
import java.util.List;

/**
 * An access control policy.
 * <p>
 * Implementations of this interface help determine whether or not specific
 * Arrowhead systems attempting to consume certain provided services are
 * authorized to do so or not.
 * <p>
 * Access policies of this type are meant to be executed <i>after</i> a
 * consuming system has been verified to have a certificate signed by a trusted
 * issuer.
 */
public interface AccessPolicy {
    /**
     * @return Access policy descriptor.
     */
    @ThreadSafe
    AccessType descriptor();

    /**
     * Determines whether or not the described {@code system} may consume the
     * described {@code service} using the given access {@code token}, if any.
     *
     * @param consumer Description of system attempting to consume the
     *                 {@code service} in question.
     * @param provider The system providing the consumed {@code service}.
     * @param service  Description of service that the {@code consumer}
     *                 attempts to consume.
     * @param token    Access token presented by the {@code consumer}, if any.
     * @return {@code true} only if {@code consumer} is permitted to consume
     * {@code service}.
     */
    @ThreadSafe
    boolean isAuthorized(
        SystemRecordWithIdentity consumer,
        ArSystem provider,
        ServiceRecord service,
        String token
    )
        throws AccessTokenException;

    /**
     * @return Access policy granting access to all consumers belong to the
     * same local cloud as the provider of the service being consumed.
     */
    @ThreadSafe
    static AccessPolicy cloud() {
        return AccessByCloudCertificate.instance;
    }

    /**
     * Creates new access policy granting access to consumers with certificate
     * chains sharing the same
     * {@link SystemIdentity master}
     * certificate as the provider of the service being consumed, as well as
     * being able to present a token from an authorization system that must be
     * resolved at some later point.
     * <p>
     * Authorization system resolution could be performed, for example, by a
     * {@link se.arkalix.plugin.Plugin plugin}.
     * <p>
     * Note that access policy instances of this type can be shared by multiple
     * services.
     *
     * @return New token access policy.
     */
    @ThreadSafe
    static AccessPolicy token() {
        return new AccessByToken();
    }

    /**
     * Creates new access policy granting access to consumers with certificate
     * chains sharing the same
     * {@link SystemIdentity master}
     * certificate as the provider of the service being consumed, as well as
     * being able to present a token from the authorization system represented
     * by the given public key.
     * <p>
     * Note that access policy instances of this type can be shared by multiple
     * services.
     *
     * @return New token access policy.
     */
    @ThreadSafe
    static AccessPolicy token(final PublicKey authorizationKey) {
        return new AccessByToken(authorizationKey);
    }

    /**
     * Creates new access policy only granting access to consumers from the
     * same local cloud as the provider of the service being consumed, as well
     * as being named in the given white-list.
     * <p>
     * Note that the white-listed names are not full names. Only the system
     * name parts, as described
     * {@link SystemIdentity here}.
     *
     * @param whitelist Names of systems to be allowed access.
     * @return Created access policy.
     */
    @ThreadSafe
    static AccessPolicy whitelist(final String... whitelist) {
        return whitelist(List.of(whitelist));
    }

    /**
     * Creates new access policy only granting access to consumers originating
     * from the same cloud as the provider of the service being consumed, as
     * well as being named in the white-list.
     * <p>
     * Note that the white-listed names are not full names. Only the system
     * name parts, as described
     * {@link SystemIdentity here}.
     * <p>
     * Also note that access policy instances of this type can be shared by
     * multiple services.
     *
     * @param whitelist Collection of names of systems to be allowed access.
     * @return Created access policy.
     */
    @ThreadSafe
    static AccessPolicy whitelist(final Collection<String> whitelist) {
        return new AccessByCloudWhitelist(whitelist);
    }

    /**
     * @return Access policy granting unrestricted access. Use of this access
     * policy is only allowed for systems running in insecure mode.
     */
    @ThreadSafe
    static AccessPolicy unrestricted() {
        return AccessUnrestricted.instance;
    }
}
