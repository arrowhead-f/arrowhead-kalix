package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.net.http.HttpMethod;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link HttpService} exception catcher, useful for handling errors
 * occurring while receiving or responding to HTTP requests.
 */
public class HttpCatcher<T extends Throwable> {
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
        this.exceptionClass = exceptionClass;
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
     * Makes this catcher handle the given exception.
     *
     * @param throwable Exception causing this handler to be invoked.
     * @param request   Information about the incoming HTTP request, including
     *                  its header and body.
     * @param response  An object useful for indicating how the original
     *                  request is to be responded to.
     * @throws Exception Whatever exception the handle may want to throw.
     */
    public void handle(final Throwable throwable, final HttpServiceRequest request, final HttpServiceResponse response) throws Exception {
        handler.handle(throwable, request, response);
    }
}
