package se.arkalix.internal.net.http.service;

import se.arkalix.util.annotation.Internal;

import java.util.Optional;

/**
 * A function useful for determining what {@link HttpServerService} to use for
 * handling a request with some given path.
 */
@Internal
@FunctionalInterface
public interface HttpServiceLookup {
    /**
     * Tries to determine what {@link HttpServerService} to use for handling an
     * incoming HTTP request with the given {@code path}.
     *
     * @param path Path of incoming HTTP request to be handled by service.
     * @return Designated service, if any.
     */
    Optional<HttpServerService> getServiceByPath(final String path);
}
