package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.net.http.HttpMethod;

import java.util.Optional;

/**
 * Describes the conditions for and where to route matching incoming
 * {@link HttpServiceRequest}s.
 */
public class HttpRoute {
    private final HttpMethod method;
    private final HttpPattern pattern;
    private final HttpServiceHandler handler;

    /**
     * Creates new {@link HttpService} route.
     *
     * @param method  HTTP method to require for given requests to match this
     *                route. Use {@code null} to allow any method.
     * @param pattern HTTP pattern to require for given request paths to match
     *                this route. Use {@code null} to allow any path.
     * @param handler The handler to execute with matching requests.
     */
    public HttpRoute(final HttpMethod method, final HttpPattern pattern, final HttpServiceHandler handler) {
        this.method = method;
        this.pattern = pattern;
        this.handler = handler;
    }

    /**
     * @return {@link HttpMethod}, if any, that routed requests must match.
     */
    public Optional<HttpMethod> method() {
        return Optional.ofNullable(method);
    }

    /**
     * @return {@link HttpPattern}, if any, that the paths of routed request
     * must match.
     */
    public Optional<HttpPattern> pattern() {
        return Optional.ofNullable(pattern);
    }

    /**
     * @return The handler to execute with a given {@link HttpServiceRequest}
     * if the conditions of this route are satisfied.
     */
    public HttpServiceHandler handler() {
        return handler;
    }
}
