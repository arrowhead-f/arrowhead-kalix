package se.arkalix.security.access;

import se.arkalix.descriptor.SecurityDescriptor;

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
     * @return Security policy descriptor.
     */
    SecurityDescriptor security();
}
