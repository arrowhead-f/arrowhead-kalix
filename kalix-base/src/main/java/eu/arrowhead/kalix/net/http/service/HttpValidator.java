package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.net.http.HttpMethod;

import java.util.Optional;

/**
 * A {@link HttpService} validator, useful for verifying and/or responding to
 * incoming HTTP requests before they are provided to their target
 * {@link HttpRoute}s.
 * <p>
 * Note that filters cannot modify requests.
 */
public class HttpValidator {
    private final int ordinal;
    private final HttpMethod method;
    private final HttpPattern pattern;
    private final HttpValidatorHandler handler;

    /**
     * Creates new {@link HttpService} filter.
     *
     * @param ordinal When to execute the filter relative to other filters.
     *                Lower numbers are executed first.
     * @param method  HTTP method to require in filtered requests. Use
     *                {@code null} to allow any method.
     * @param pattern HTTP pattern to require filtered request paths to match.
     *                Use {@code null} to allow any path.
     * @param handler The handler to execute with matching requests.
     */
    public HttpValidator(
        final int ordinal,
        final HttpMethod method,
        final HttpPattern pattern,
        final HttpValidatorHandler handler)
    {
        this.ordinal = ordinal;
        this.method = method;
        this.pattern = pattern;
        this.handler = handler;
    }

    /**
     * @return Integer indicating when to execute this filter in relation to
     * other such. Filters with lower ordinals are to be executed first.
     */
    public int ordinal() {
        return ordinal;
    }

    /**
     * @return {@link HttpMethod}, if any, that filtered requests must match
     * for this filter to be invoked.
     */
    public Optional<HttpMethod> method() {
        return Optional.ofNullable(method);
    }

    /**
     * @return {@link HttpPattern}, if any, that filtered request paths must
     * match for this filter to be invoked.
     */
    public Optional<HttpPattern> pattern() {
        return Optional.ofNullable(pattern);
    }

    /**
     * Allows this filter to handle the given incoming HTTP request.
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
    public void handle(HttpServiceRequest request, HttpServiceResponse response) throws Exception {
        handler.handle(request, response);
    }
}
