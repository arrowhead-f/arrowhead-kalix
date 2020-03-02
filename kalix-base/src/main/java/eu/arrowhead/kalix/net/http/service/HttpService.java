package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.net.http.HttpMethod;

import java.util.ArrayList;

public class HttpService implements HttpServiceHandler {
    private final ArrayList<HttpFilter> filters;
    private final ArrayList<HttpRoute> routes;

    private HttpService(final Builder builder) {
        filters = builder.filters;
        routes = builder.routes;
    }

    @Override
    public void handle(final HttpServiceRequest request, final HttpServiceResponse response) throws Exception {
        // TODO.
    }

    public static class Builder {
        private final ArrayList<HttpFilter> filters = new ArrayList<>(0);
        private final ArrayList<HttpRoute> routes = new ArrayList<>(0);

        public Builder addFilter(final HttpFilter filter) {
            filters.add(filter);
            return this;
        }

        public Builder before(final HttpMethod method, final HttpPattern pattern, final HttpServiceHandler handler) {
            return addFilter(new HttpFilter(-1000, method, pattern, handler));
        }

        public Builder before(final HttpMethod method, final String pattern, final HttpServiceHandler handler) {
            return before(method, HttpPattern.valueOf(pattern), handler);
        }

        public Builder before(final HttpPattern pattern, final HttpServiceHandler handler) {
            return before(null, pattern, handler);
        }

        public Builder before(final String pattern, final HttpServiceHandler handler) {
            return before(null, HttpPattern.valueOf(pattern), handler);
        }

        public Builder before(final HttpMethod method, final HttpServiceHandler handler) {
            return before(method, (HttpPattern) null, handler);
        }

        public Builder before(final HttpServiceHandler handler) {
            return before(null, (HttpPattern) null, handler);
        }

        public Builder after(final HttpMethod method, final HttpPattern pattern, final HttpServiceHandler handler) {
            return addFilter(new HttpFilter(1000, method, pattern, handler));
        }

        public Builder after(final HttpMethod method, final String pattern, final HttpServiceHandler handler) {
            return after(method, HttpPattern.valueOf(pattern), handler);
        }

        public Builder after(final HttpPattern pattern, final HttpServiceHandler handler) {
            return after(null, pattern, handler);
        }

        public Builder after(final String pattern, final HttpServiceHandler handler) {
            return after(null, HttpPattern.valueOf(pattern), handler);
        }

        public Builder after(final HttpMethod method, final HttpServiceHandler handler) {
            return after(method, (HttpPattern) null, handler);
        }

        public Builder after(final HttpServiceHandler handler) {
            return before(null, (HttpPattern) null, handler);
        }

        public Builder addRoute(final HttpRoute route) {
            routes.add(route);
            return this;
        }

        public Builder addRoute(final HttpMethod method, final HttpPattern pattern, final HttpServiceHandler handler) {
            return addRoute(new HttpRoute(method, pattern, handler));
        }

        public Builder addRoute(final HttpMethod method, final String pattern, final HttpServiceHandler handler) {
            return addRoute(method, HttpPattern.valueOf(pattern), handler);
        }

        public Builder get(final String path, final HttpServiceHandler handler) {
            return addRoute(HttpMethod.GET, path, handler);
        }

        public Builder post(final String path, final HttpServiceHandler handler) {
            return addRoute(HttpMethod.POST, path, handler);
        }

        public Builder put(final String path, final HttpServiceHandler handler) {
            return addRoute(HttpMethod.PUT, path, handler);
        }

        public Builder delete(final String path, final HttpServiceHandler handler) {
            return addRoute(HttpMethod.DELETE, path, handler);
        }

        public Builder head(final String path, final HttpServiceHandler handler) {
            return addRoute(HttpMethod.HEAD, path, handler);
        }

        public Builder options(final String path, final HttpServiceHandler handler) {
            return addRoute(HttpMethod.OPTIONS, path, handler);
        }

        public Builder connect(final String path, final HttpServiceHandler handler) {
            return addRoute(HttpMethod.CONNECT, path, handler);
        }

        public Builder patch(final String path, final HttpServiceHandler handler) {
            return addRoute(HttpMethod.PATCH, path, handler);
        }

        public Builder trace(final String path, final HttpServiceHandler handler) {
            return addRoute(HttpMethod.TRACE, path, handler);
        }

        public HttpService build() {
            return new HttpService(this);
        }
    }
}
