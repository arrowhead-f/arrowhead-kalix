package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A {@link HttpService} validator, useful for verifying and/or responding to
 * incoming HTTP requests before they are provided to their designated
 * {@link HttpRoute}s.
 */
public class HttpValidator implements Comparable<HttpValidator> {
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
     * Determines whether there are method/path pairs that would match both
     * this validator and the provided route.
     *
     * @param route Route to test.
     * @return {@code true} only if an intersection of matching method/path
     * pairs exists between this catcher and the given route.
     */
    public boolean matchesIntersectionOf(final HttpRoute route) {
        if (method != null) {
            final var method0 = route.method();
            if (method0.isPresent() && method0.get() != method) {
                return false;
            }
        }
        if (pattern != null) {
            final var pattern0 = route.pattern();
            return pattern0.isEmpty() || pattern.intersectsWith(pattern0.get());
        }
        return true;
    }

    /**
     * Offers this validator the opportunity to respond to this request. If it
     * does, it is assumed to the request needs no more processing and a
     * response is to be immediately scheduled to the requester.
     * <p>
     * A validator will typically only handle a request if it is invalid.
     *
     * @param request  Information about the incoming HTTP request, including
     *                 its header and body.
     * @param response An object useful for indicating how the request is to be
     *                 responded to.
     * @return Future completed when validation is complete. Its value is
     * {@code true} only if the request was handled.
     * @throws Exception Whatever exception the handle may want to throw. If
     *                   the HTTP service owning this handle knows how to
     *                   translate the exception into a certain kind of HTTP
     *                   response, it should. Otherwise the requester should
     *                   receive a 500 Internal Server Error response without
     *                   any details and the exception be logged (if logging is
     *                   enabled).
     */
    public Future<Boolean> tryHandle(final HttpServiceRequest request, final HttpServiceResponse response)
        throws Exception
    {
        mismatch:
        {
            if (method != null && !method.equals(request.method())) {
                break mismatch;
            }
            final List<String> pathParameters;
            if (pattern != null) {
                pathParameters = new ArrayList<>(pattern.nParameters());
                if (!pattern.match(request.path(), pathParameters)) {
                    break mismatch;
                }
            }
            else {
                pathParameters = Collections.emptyList();
            }
            return handler.handle(request.wrapHeadWithPathParameters(pathParameters), response)
                .map(ignored -> response.body().isPresent() || response.status().isPresent());
        }
        return Future.success(false);
    }

    @Override
    public int compareTo(final HttpValidator other) {
        if (method != null) {
            if (other.method == null) {
                return 1;
            }
            final var c0 = method.compareTo(other.method);
            if (c0 != 0) {
                return c0;
            }
        }
        else if (other.method == null) {
            return -1;
        }
        if (pattern != null) {
            if (other.pattern == null) {
                return 1;
            }
            final var c1 = pattern.compareTo(other.pattern);
            if (c1 != 0) {
                return c1;
            }
        }
        return ordinal - other.ordinal;
    }
}
