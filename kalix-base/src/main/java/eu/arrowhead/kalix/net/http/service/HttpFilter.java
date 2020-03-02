package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.net.http.HttpMethod;

import java.util.Optional;

/**
 * A {@link HttpService} filter, useful for verifying incoming HTTP requests
 * before or after they are handled, cancelling or responding to pending
 * responses, or modifying response headers.
 * <p>
 * Note that filters cannot modify requests.
 */
public class HttpFilter implements HttpServiceHandler {
    private final int ordinal;
    private final HttpMethod method;
    private final HttpPattern pattern;
    private final HttpServiceHandler handler;

    /**
     * Creates new {@link HttpService} filter.
     *
     * @param ordinal When to execute the filter relative to other filters and
     *                the {@link HttpRoute} it filters. Lower numbers are
     *                executed first. The filtered {@link HttpRoute} is
     *                executed after ordinal 0 and before ordinal 1.
     * @param method  HTTP method to require in filtered requests. Use
     *                {@code null} to allow any method.
     * @param pattern HTTP pattern to require filtered request paths to match.
     *                Use {@code null} to allow any path.
     * @param handler The handler to execute with matching requests.
     */
    public HttpFilter(
        final int ordinal,
        final HttpMethod method,
        final HttpPattern pattern,
        final HttpServiceHandler handler
    ) {
        this.ordinal = ordinal;
        this.method = method;
        this.pattern = pattern;
        this.handler = handler;
    }

    /**
     * @return Integer indicating when to execute filter in relation to other
     * filters and a {@link HttpRoute} handler.
     */
    public int ordinal() {
        return ordinal;
    }

    /**
     * @return {@link HttpMethod}, if any, that filtered requests must match.
     */
    public Optional<HttpMethod> method() {
        return Optional.ofNullable(method);
    }

    /**
     * @return {@link HttpPattern}, if any, that filtered request paths must
     * match.
     */
    public Optional<HttpPattern> pattern() {
        return Optional.ofNullable(pattern);
    }

    @Override
    public void handle(HttpServiceRequest request, HttpServiceResponse response) throws Exception {
        handler.handle(request, response);
    }
}
