package eu.arrowhead.kalix.net;

import java.util.Optional;

/**
 * Matches URI paths with {@link PathHandler}s.
 */
public interface PathRouter<Request, Response> {
    /**
     * Attempts to resolve a given URI path into a {@link PathHandler} associated
     * with that path.
     *
     * @param path URI path to resolve.
     * @return A matching path, with any collected path parameters, or nothing.
     */
    Optional<PathRouteAndParameters<Request, Response>> resolve(final String path);
}
