package eu.arrowhead.kalix.http;

import java.util.Optional;

/**
 * A {@link HttpService} filter, useful for verifying HTTP requests before or
 * after they are handled, cancelling or responding to pending responses, or
 * modifying response headers.
 * <p>
 * Note that filters cannot modify requests. They can reply to requests with
 * their own response bodies, however. If a filter executed before a
 * {@link HttpServiceRoute} handler returns any other value than null or a
 * {@link eu.arrowhead.kalix.concurrent.Future} yielding {@link Void}, no more
 * handlers are invoked and the original request is responded to as soon as
 * the returned value has been serialized into a response body.
 * <p>
 * Deserialization and serialization of request and response bodies is taken
 * care of by the {@link HttpService} handling the filter and the handler it
 * filters.
 */
public class HttpServiceFilter {
    private final int ordinal;
    private final HttpMethod method;
    private final HttpPattern pattern;
    private final HttpServiceHandler handler;

    /**
     * Creates new {@link HttpService} filter.
     *
     * @param ordinal When to execute the filter relative to other filters and
     *                the {@link HttpServiceRoute} it filters. Lower numbers are
     *                executed first. The filtered {@link HttpServiceRoute} is
     *                executed after ordinal 0 and before ordinal 1.
     * @param method  HTTP method to require in filtered requests. Use
     *                {@code null} to allow any method.
     * @param pattern HTTP pattern to require filtered request paths to match.
     *                Use {@code null} to allow any path.
     * @param handler The handler to execute with matching requests.
     */
    public HttpServiceFilter(
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
     * filters and a {@link HttpServiceRoute} handler.
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

    /**
     * @return The handler to execute when all preconditions for this filter
     * are satisfied.
     */
    public HttpServiceHandler handler() {
        return handler;
    }
}
