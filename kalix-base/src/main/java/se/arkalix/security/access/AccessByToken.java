package se.arkalix.security.access;

import se.arkalix.description.ServiceDescription;
import se.arkalix.description.SystemDescription;

public interface AccessByToken extends AccessPolicy {
    /**
     * Determines whether or not the described {@code system} may consume the
     * described {@code service} using the given access {@code token}, if any.
     *
     * @param consumer Description of system attempting to consume the
     *                 {@code service} in question.
     * @param service  Description of service that the {@code consumer}
     *                 attempts to consume.
     * @param token    Access token presented by the {@code consumer}, if any.
     * @return {@code true} only if {@code consumer} is permitted to consume
     * {@code service}.
     */
    boolean isAuthorized(SystemDescription consumer, ServiceDescription service, byte[] token) throws Exception;
}
