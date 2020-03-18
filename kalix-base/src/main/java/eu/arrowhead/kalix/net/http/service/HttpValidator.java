package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.internal.net.http.service.HttpServiceInternal;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A {@link HttpServiceInternal} validator, useful for verifying and/or responding to
 * incoming HTTP requests before they are provided to their designated
 * {@link HttpRoute}s.
 */
public class HttpValidator implements HttpRoutable {
    private final int ordinal;
    private final HttpMethod method;
    private final HttpPattern pattern;
    private final HttpValidatorHandler handler;

    /**
     * Creates new {@link HttpServiceInternal} filter.
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
     * @return Integer indicating when to execute this validator in relation to
     * other catchers that match the same {@link HttpMethod methods} and
     * have the exact same {@link HttpPattern patterns}.
     */
    public int ordinal() {
        return ordinal;
    }

    /**
     * @return {@link HttpMethod}, if any, that requests causing thrown
     * exceptions must match for this validator to be invoked.
     */
    @Override
    public Optional<HttpMethod> method() {
        return Optional.ofNullable(method);
    }

    /**
     * @return {@link HttpPattern}, if any, that paths of requests causing
     * thrown exceptions must match for this validator to be invoked.
     */
    @Override
    public Optional<HttpPattern> pattern() {
        return Optional.ofNullable(pattern);
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
     */
    public Future<Boolean> tryHandle(final HttpRouteTask task) {
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
            try {
                return handler.handle(task.request().cloneAndSet(pathParameters), response)
                    .map(ignored -> response.status().isPresent());
            }
            catch (final Throwable throwable) {
                return Future.failure(throwable);
            }
        }
        return Future.success(false);
    }
}
