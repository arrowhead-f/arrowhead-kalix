package eu.arrowhead.kalix.http;

import java.util.Optional;

public class HttpFilter {
    private final int order;
    private final HttpMethod method;
    private final String pattern;
    private final HttpHandler handler;

    public HttpFilter(final int order, final HttpMethod method, final String pattern, final HttpHandler handler) {
        this.order = order;
        this.method = method;
        this.pattern = pattern;
        this.handler = handler;
    }

    public int order() {
        return order;
    }

    public Optional<HttpMethod> method() {
        return Optional.ofNullable(method);
    }

    public Optional<String> pattern() {
        return Optional.ofNullable(pattern);
    }

    public HttpHandler handler() {
        return handler;
    }
}
