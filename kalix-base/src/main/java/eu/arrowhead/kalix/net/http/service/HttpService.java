package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HttpService {
    private final List<HttpRouteSequence> routeSequences;

    private HttpService(final Builder builder) {
        final var routeSequenceFactory = new HttpRouteSequenceFactory(builder.catchers, builder.validators);
        routeSequences = builder.routes.stream()
            .map(routeSequenceFactory::createRouteSequenceFor)
            .collect(Collectors.toList());
    }

    public Future<?> handle(final HttpServiceRequest request, final HttpServiceResponse response) {
        return Future
            .flatReducePlain(routeSequences, false, (isHandled, routeSequence) -> {
                if (isHandled) {
                    return Future.success(true);
                }
                return routeSequence.tryHandle(request, response);
            })
            .flatMap(isHandled -> {
                if (!isHandled) {
                    response
                        .status(HttpStatus.NOT_FOUND)
                        .headers(new HttpHeaders())
                        .body(new byte[0]);
                }
                return null;
            });
    }

    public static class Builder {
        private final ArrayList<HttpCatcher<?>> catchers = new ArrayList<>(0);
        private final ArrayList<HttpRoute> routes = new ArrayList<>(0);
        private final ArrayList<HttpValidator> validators = new ArrayList<>(0);
        private int filterOrdinal = 0;
        private int catcherOrdinal = 0;

        public Builder catcher(final HttpCatcher<?> catcher) {
            catchers.add(catcher);
            return this;
        }

        public <T extends Throwable> Builder catcher(
            final HttpMethod method,
            final String pattern,
            final Class<T> exceptionClass,
            final HttpCatcherHandler<T> handler)
        {
            return catcher(new HttpCatcher<>(
                catcherOrdinal++,
                method,
                pattern != null
                    ? HttpPattern.valueOf(pattern)
                    : null,
                exceptionClass,
                handler));
        }

        public Builder catcher(final HttpMethod method, final String pattern, final HttpCatcherHandler<?> handler) {
            return catcher(method, pattern, null, handler);
        }

        public Builder catcher(final String pattern, final HttpCatcherHandler<?> handler) {
            return catcher(null, pattern, null, handler);
        }

        public <T extends Throwable> Builder catcher(
            final String pattern,
            final Class<T> exceptionClass,
            final HttpCatcherHandler<T> handler)
        {
            return catcher(null, pattern, exceptionClass, handler);
        }

        public Builder catcher(final HttpMethod method, final HttpCatcherHandler<?> handler) {
            return catcher(method, null, null, handler);
        }

        public <T extends Throwable> Builder catcher(
            final HttpMethod method,
            final Class<T> exceptionClass,
            final HttpCatcherHandler<T> handler)
        {
            return catcher(method, null, exceptionClass, handler);
        }

        public <T extends Throwable> Builder catcher(
            final Class<T> exceptionClass,
            final HttpCatcherHandler<T> handler)
        {
            return catcher(null, null, exceptionClass, handler);
        }

        public Builder catcher(final HttpCatcherHandler<?> handler) {
            return catcher(null, null, null, handler);
        }

        public Builder validator(final HttpValidator validator) {
            validators.add(validator);
            return this;
        }

        public Builder validator(final HttpMethod method, final String pattern, final HttpValidatorHandler handler) {
            return validator(new HttpValidator(filterOrdinal++, method, pattern != null
                ? HttpPattern.valueOf(pattern)
                : null, handler));
        }

        public Builder validator(final String pattern, final HttpValidatorHandler handler) {
            return validator(null, pattern, handler);
        }

        public Builder validator(final HttpMethod method, final HttpValidatorHandler handler) {
            return validator(method, null, handler);
        }

        public Builder validator(final HttpValidatorHandler handler) {
            return validator(null, null, handler);
        }

        public Builder route(final HttpRoute route) {
            routes.add(route);
            return this;
        }

        public Builder route(final HttpMethod method, final String pattern, final HttpRouteHandler handler) {
            return route(new HttpRoute(method, HttpPattern.valueOf(pattern), handler));
        }

        public Builder get(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.GET, path, handler);
        }

        public Builder post(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.POST, path, handler);
        }

        public Builder put(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.PUT, path, handler);
        }

        public Builder delete(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.DELETE, path, handler);
        }

        public Builder head(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.HEAD, path, handler);
        }

        public Builder options(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.OPTIONS, path, handler);
        }

        public Builder connect(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.CONNECT, path, handler);
        }

        public Builder patch(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.PATCH, path, handler);
        }

        public Builder trace(final String path, final HttpRouteHandler handler) {
            return route(HttpMethod.TRACE, path, handler);
        }

        public HttpService build() {
            return new HttpService(this);
        }
    }
}
