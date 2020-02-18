package eu.arrowhead.kalix.http;

import eu.arrowhead.kalix.Service;

import java.util.ArrayList;

public class HttpService implements Service {
    private HttpService(final Builder builder) {

    }

    public static class Builder {
        private final ArrayList<HttpFilter> filters = new ArrayList<>(0);
        private final ArrayList<HttpRoute> routes = new ArrayList<>(0);


        public Builder addFilter(final HttpFilter filter) {
            filters.add(filter);
            return this;
        }

        public Builder before(final HttpMethod method, final String path, final HttpHandler handler) {
            return addFilter(new HttpFilter(-1000, method, path, handler));
        }

        public Builder before(final String path, final HttpHandler handler) {
            return addFilter(new HttpFilter(-1000, null, path, handler));
        }

        public Builder before(final HttpHandler handler) {
            return addFilter(new HttpFilter(-1000, null, null, handler));
        }

        public Builder after(final HttpMethod method, final String path, final HttpHandler handler) {
            return addFilter(new HttpFilter(1000, method, path, handler));
        }

        public Builder after(final String path, final HttpHandler handler) {
            return addFilter(new HttpFilter(1000, null, path, handler));
        }

        public Builder after(final HttpHandler handler) {
            return addFilter(new HttpFilter(1000, null, null, handler));
        }

        public Builder lastly(final HttpMethod method, final String path, final HttpHandler handler) {
            return addFilter(new HttpFilter(2000, method, path, handler));
        }

        public Builder lastly(final String path, final HttpHandler handler) {
            return addFilter(new HttpFilter(2000, null, path, handler));
        }

        public Builder lastly(final HttpHandler handler) {
            return addFilter(new HttpFilter(2000, null, null, handler));
        }

        public Builder addRoute(final HttpRoute route) {
            routes.add(route);
            return this;
        }

        public Builder get(final String path, final HttpHandler handler) {
            return addRoute(new HttpRoute(HttpMethod.GET, path, handler));
        }

        public Builder post(final String path, final HttpHandler handler) {
            return addRoute(new HttpRoute(HttpMethod.POST, path, handler));
        }

        public Builder put(final String path, final HttpHandler handler) {
            return addRoute(new HttpRoute(HttpMethod.PUT, path, handler));
        }

        public Builder delete(final String path, final HttpHandler handler) {
            return addRoute(new HttpRoute(HttpMethod.DELETE, path, handler));
        }

        public Builder head(final String path, final HttpHandler handler) {
            return addRoute(new HttpRoute(HttpMethod.HEAD, path, handler));
        }

        public Builder options(final String path, final HttpHandler handler) {
            return addRoute(new HttpRoute(HttpMethod.OPTIONS, path, handler));
        }

        public Builder connect(final String path, final HttpHandler handler) {
            return addRoute(new HttpRoute(HttpMethod.CONNECT, path, handler));
        }

        public Builder patch(final String path, final HttpHandler handler) {
            return addRoute(new HttpRoute(HttpMethod.PATCH, path, handler));
        }

        public Builder trace(final String path, final HttpHandler handler) {
            return addRoute(new HttpRoute(HttpMethod.TRACE, path, handler));
        }

        public HttpService build() {
            return new HttpService(this);
        }
    }
}
