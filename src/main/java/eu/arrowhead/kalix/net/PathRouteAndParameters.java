package eu.arrowhead.kalix.net;

import eu.arrowhead.kalix.collections.UnsortedListMap;

import java.util.Map;
import java.util.function.Consumer;

final class PathRouteAndParameters<Request, Response> {
    private final UnsortedListMap<String, String> queryParameters;
    private final Consumer<PathHandler<Request, Response>> callback;

    PathRouteAndParameters(final Consumer<PathHandler<Request, Response>> callback) {
        this.queryParameters = new UnsortedListMap<>();
        this.callback = callback;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public void putQueryParameter(final String name, final String value) {
        queryParameters.put(name, value);
    }

    public void callback(final PathHandler<Request, Response> route) {
        callback.accept(route);
    }
}
