package se.arkalix.net.http.service;

import se.arkalix.net.http.HttpMethod;
import se.arkalix.util.concurrent.Future;

import java.util.*;

/**
 * A {@link HttpService} exception catcher, useful for handling errors
 * occurring while receiving or responding to HTTP requests.
 */
public class HttpCatcher<T extends Throwable> implements HttpRoutable {
    private final int ordinal;
    private final HttpMethod method;
    private final HttpPattern pattern;
    private final Class<T> exceptionClass;
    private final HttpCatcherHandler<T> handler;

    /**
     * Creates new {@link HttpService} exception catcher.
     *
     * @param ordinal        When to execute the catcher relative to other
     *                       catchers. Lower numbers are executed first.
     * @param method         HTTP method that must be matched by caught request
     *                       exceptions. Use {@code null} to allow any method.
     * @param pattern        HTTP pattern that must be matched by caught
     *                       request exceptions. Use {@code null} to allow any
     *                       path.
     * @param exceptionClass Type caught exceptions must be assignable to. Use
     *                       {@code null} to allow all exceptions.
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
     * other catchers that match the same {@link HttpMethod methods}, {@link
     * HttpPattern patterns} and {@link Throwable exception}.
     */
    public int ordinal() {
        return ordinal;
    }

    /**
     * @return {@link HttpMethod}, if any, that requests causing thrown
     * exceptions must match for this catcher to be invoked.
     */
    @Override
    public Optional<HttpMethod> method() {
        return Optional.ofNullable(method);
    }

    /**
     * @return {@link HttpPattern}, if any, that paths of requests causing
     * thrown exceptions must match for this catcher to be invoked.
     */
    @Override
    public Optional<HttpPattern> pattern() {
        return Optional.ofNullable(pattern);
    }

    /**
     * @return Class that caught exceptions must be assignable to for this
     * catcher to be invoked.
     */
    public Class<T> exceptionClass() {
        return exceptionClass;
    }

    /**
     * Tries to make this catcher handle the given exception.
     * <p>
     * The exception will only be handled if the given task request matches the
     * method, pattern and exception class of this catcher, and the handler
     * this class wraps reports that it did handle it.
     *
     * @param throwable Exception causing this handler to be invoked.
     * @param task      Incoming HTTP request route task.
     * @return Future completed when catching is complete. Its value is
     * {@code true} only if the throwable was handled.
     */
    public Future<Boolean> tryHandle(final Throwable throwable, final HttpRouteTask task) {
        mismatch:
        {
            if (method != null && !method.equals(task.request().method())) {
                break mismatch;
            }
            if (!exceptionClass.isAssignableFrom(throwable.getClass())) {
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
            try {
                return handler.handle(exceptionClass.cast(throwable),
                    task.request().cloneAndSet(pathParameters), response)
                    .map(ignored -> response.status().isPresent());
            }
            catch (final Throwable throwable1) {
                throwable1.addSuppressed(throwable);
                return Future.failure(throwable);
            }
        }
        return Future.success(false);
    }
}
