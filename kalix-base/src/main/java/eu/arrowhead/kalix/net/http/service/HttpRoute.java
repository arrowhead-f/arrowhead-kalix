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
    private final HttpRouteHandler handler;

    /**
     * Creates new {@link HttpService} route.
     *
     * @param method  HTTP method to require for given requests to match this
     *                route. Use {@code null} to allow any method.
     * @param pattern HTTP pattern to require for given request paths to match
     *                this route. Use {@code null} to allow any path.
     * @param handler The handler to execute with matching requests.
     */
    public HttpRoute(final HttpMethod method, final HttpPattern pattern, final HttpRouteHandler handler) {
        this.method = method;
        this.pattern = pattern;
        this.handler = handler;
    }

    /**
     * @return {@link HttpMethod}, if any, that routed requests must match
     * for this route to be invoked.
     */
    public Optional<HttpMethod> method() {
        return Optional.ofNullable(method);
    }

    /**
     * @return {@link HttpPattern}, if any, that the paths of routed requests
     * must match for this route to be invoked.
     */
    public Optional<HttpPattern> pattern() {
        return Optional.ofNullable(pattern);
    }

    /**
     * Makes this route handle the given HTTP request.
     *
     * @param request  Information about the incoming HTTP request, including
     *                 its header and body.
     * @param response An object useful for indicating how the request is to be
     *                 responded to.
     * @throws Exception Whatever exception the handle may want to throw. If
     *                   the HTTP service owning this handle knows how to
     *                   translate the exception into a certain kind of HTTP
     *                   response, it should. Otherwise the requester should
     *                   receive a 500 Internal Server Error response without
     *                   any details and the exception be logged (if logging is
     *                   enabled).
     */
    public void handle(final HttpServiceRequest request, final HttpServiceResponse response) throws Exception {
        handler.handle(request, response);
    }
}
