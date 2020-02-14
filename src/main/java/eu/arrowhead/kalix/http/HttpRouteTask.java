package eu.arrowhead.kalix.http;

import eu.arrowhead.kalix.collections.UnsortedListMap;

import java.util.Map;
import java.util.function.Consumer;

final class HttpRouteTask {
    private final UnsortedListMap<String, String> queryParameters;
    private final Consumer<HttpRoute> callback;

    HttpRouteTask(final Consumer<HttpRoute> callback) {
        this.queryParameters = new UnsortedListMap<>();
        this.callback = callback;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public void putQueryParameter(final String name, final String value) {
        queryParameters.put(name, value);
    }

    public void callback(final HttpRoute route) {
        callback.accept(route);
    }
}
