package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.internal.net.http.service.HttpPattern;
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
     * Offers this validator the opportunity to respond to the request in the
     * given task. If it does respond, it is assumed that the request needs no
     * more processing and a response is sent to the requester immediately.
     * <p>
     * A validator will typically only handle a request if it is invalid.
     *
     * @param task Incoming HTTP request route task.
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
    public Future<Boolean> tryHandle(final HttpRouteTask task) throws Exception {
        mismatch:
        {
            if (method != null && !method.equals(task.request().method())) {
                break mismatch;
            }
            final List<String> pathParameters;
            if (pattern != null) {
                pathParameters = new ArrayList<>(pattern.nParameters());
                if (!pattern.match(task.request().path(), task.basePath().length(), pathParameters)) {
                    break mismatch;
                }
            }
            else {
                pathParameters = Collections.emptyList();
            }
            final var response = task.response();
            return handler.handle(task.request().cloneAndSet(pathParameters), response)
                .map(ignored -> response.status().isPresent());
        }
        return Future.success(false);
    }

    @Override
    public int compareTo(final HttpValidator other) {
        // Validators with patterns come before those without patterns.
        if (pattern != null) {
            if (other.pattern == null) {
                return -1;
            }
            final var c = pattern.compareTo(other.pattern);
            if (c != 0) {
                return c;
            }
        }
        else if (other.pattern != null) {
            return 1;
        }

        // Validators with methods come before those without methods.
        if (method != null) {
            if (other.method == null) {
                return -1;
            }
            final var c = method.compareTo(other.method);
            if (c != 0) {
                return c;
            }
        }
        else if (other.method != null) {
            return 1;
        }

        // If nothing else remains, use ordinals to decide order.
        return ordinal - other.ordinal;
    }
}
