package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.util.*;

/**
 * A {@link HttpService} exception catcher, useful for handling errors
 * occurring while receiving or responding to HTTP requests.
 */
public class HttpCatcher<T extends Throwable> implements Comparable<HttpCatcher<?>> {
    private final int ordinal;
    private final HttpMethod method;
    private final HttpPattern pattern;
    private final Class<T> exceptionClass;
    private final HttpCatcherHandler<T> handler;

    /**
     * Creates new {@link HttpService} exception catcher.
     *
     * @param ordinal        When to execute the filter relative to other
     *                       filters. Lower numbers are executed first.
     * @param method         HTTP method to require in filtered requests. Use
     *                       {@code null} to allow any method.
     * @param pattern        HTTP pattern to require filtered request paths to
     *                       match. Use {@code null} to allow any path.
     * @param exceptionClass Type caught exceptions must be assignable to. Use
     *                       {@code null} to catch all exceptions.
     * @param handler        The handler to execute with matching requests.
     */
    public HttpCatcher(
        final int ordinal,
        final HttpMethod method,
        final HttpPattern pattern,
        final Class<T> exceptionClass,
        final HttpCatcherHandler<T> handler)
    {
        this.ordinal = ordinal;
        this.method = method;
        this.pattern = pattern;
        this.exceptionClass = Objects.requireNonNull(exceptionClass, "Expected exceptionClass");
        this.handler = Objects.requireNonNull(handler, "Expected handler");
    }

    /**
     * @return Integer indicating when to execute this catcher in relation to
     * other such. Catchers with lower ordinals are to be executed first.
     */
    public int ordinal() {
        return ordinal;
    }

    /**
     * @return {@link HttpMethod}, if any, that requests causing thrown
     * exceptions must match for this catcher to be invoked.
     */
    public Optional<HttpMethod> method() {
        return Optional.ofNullable(method);
    }

    /**
     * @return {@link HttpPattern}, if any, that paths of requests causing
     * thrown exceptions must match for this catcher to be invoked.
     */
    public Optional<HttpPattern> pattern() {
        return Optional.ofNullable(pattern);
    }

    /**
     * @return Class, if any, that caught exceptions must be assignable to for
     * this catcher to be invoked.
     */
    public Optional<Class<T>> exceptionClass() {
        return Optional.ofNullable(exceptionClass);
    }

    /**
     * Determines whether there are method/path pairs that would match both
     * this catcher and the provided route.
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
     * Tries to make this catcher handle the given exception.
     * <p>
     * The exception will only be handled if the given request matches the
     * method, pattern and exception class of this catcher, and the handler
     * this class wraps reports that it did handle it.
     *
     * @param throwable Exception causing this handler to be invoked.
     * @param request   Information about the incoming HTTP request, including
     *                  its header and body.
     * @param response  An object useful for indicating how the original
     *                  request is to be responded to.
     * @return Future completed when catching is complete. Its value is
     * {@code true} only if the throwable was handled.
     * @throws Exception Whatever exception the handle may want to throw. This
     *                   exception is only thrown if the handle is executed.
     */
    public Future<Boolean> tryHandle(
        final Throwable throwable,
        final HttpServiceRequest request,
        final HttpServiceResponse response)
        throws Exception
    {
        mismatch:
        {
            if (method != null && !method.equals(request.method())) {
                break mismatch;
            }

            if (!exceptionClass.isAssignableFrom(throwable.getClass())) {
                break mismatch;
            }
            final var throwable0 = exceptionClass.cast(throwable);

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

            return handler.handle(throwable0, request.wrapHeadWithPathParameters(pathParameters), response)
                .map(ignored -> response.body().isPresent() || response.status().isPresent());
        }
        return Future.success(false);
    }

    @Override
    public int compareTo(final HttpCatcher<?> other) {
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
        if (exceptionClass == other.exceptionClass) {
            return ordinal - other.ordinal;
        }
        if (exceptionClass.isAssignableFrom(other.exceptionClass)) {
            return -1;
        }
        if (other.exceptionClass.isAssignableFrom(exceptionClass)) {
            return 1;
        }
        throw new IllegalStateException("Either of \"" + exceptionClass +
            "\" and \"" + other.exceptionClass +
            "\" does not extend \"Throwable\"");
    }
}
