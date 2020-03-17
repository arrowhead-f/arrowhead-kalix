package eu.arrowhead.kalix.internal.net.http.service;

import eu.arrowhead.kalix.util.annotation.Internal;

import java.util.Optional;

/**
 * A function useful for determining what {@link HttpServiceInternal} to use for
 * handling a request with some given path.
 */
@Internal
@FunctionalInterface
public interface HttpServiceLookup {
    /**
     * Tries to determine what {@link HttpServiceInternal} to use for handling an
     * incoming HTTP request with the given {@code path}.
     *
     * @param path Path of incoming HTTP request to be handled by service.
     * @return Designated service, if any.
     */
    Optional<HttpServiceInternal> getServiceByPath(final String path);
}
