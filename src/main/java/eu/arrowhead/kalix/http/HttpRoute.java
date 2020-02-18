package eu.arrowhead.kalix.http;

public class HttpRoute {
    private final HttpMethod method;
    private final String pattern;
    private final HttpHandler handler;

    public HttpRoute(final HttpMethod method, final String pattern, final HttpHandler handler) {
        this.method = method;
        this.pattern = pattern;
        this.handler = handler;
    }

    public HttpMethod method() {
        return method;
    }

    public String pattern() {
        return pattern;
    }

    public HttpHandler handler() {
        return handler;
    }
}
