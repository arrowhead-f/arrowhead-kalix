package se.arkalix.security.access;

import se.arkalix.description.ServiceDescription;
import se.arkalix.description.SystemDescription;
import se.arkalix.descriptor.AccessDescriptor;

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
     * @return Access control descriptor.
     */
    AccessDescriptor descriptor();

    /**
     * Determines whether or not the described {@code system} may consume the
     * described {@code service} using the given access {@code token}, if any.
     *
     * @param system  Description of system attempting to consume
     *                {@code service}.
     * @param service Description of service that {@code system} attempts to
     *                consume.
     * @param token   Access token presented by {@code system}, if any.
     * @return {@code true} only if {@code system} is permitted to consume
     * {@code service}.
     */
    boolean isAuthorized(SystemDescription system, ServiceDescription service, byte[] token);
}
