package se.arkalix.net.http.service._internal;

import se.arkalix.net.http.service.HttpCatcher;
import se.arkalix.net.http.service.HttpRoute;
import se.arkalix.net.http.service.HttpFilter;
import se.arkalix.util.annotation.Internal;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class useful for constructing sound {@link HttpRouteSequence}
 * instances.
 */
@Internal
public class HttpRouteSequenceFactory {
    private final List<HttpCatcher<?>> catchers;
    private final List<HttpFilter> filters;

    /**
     * Creates new factory containing given {@code catchers} and
     * {@code filters}.
     * <p>
     * Subsequently created {@link HttpRouteSequence}s will contain references
     * to all catchers and filters that could, potentially, match incoming
     * HTTP requests targeted at certain {@link HttpRoute}s.
     *
     * @param catchers Route exception catchers.
     * @param filters  Route filters.
     */
    public HttpRouteSequenceFactory(final List<HttpCatcher<?>> catchers, final List<HttpFilter> filters) {
        this.catchers = new ArrayList<>(catchers);
        this.filters = new ArrayList<>(filters);

        this.catchers.sort(HttpRoutables::compareCatchers);
        this.filters.sort(HttpRoutables::compareFilters);
    }

    /**
     * Creates new {@link HttpRouteSequence} containing references to all
     * catchers and filters, known by this factory, that could, potentially,
     * match incoming HTTP requests targeted at the given {@link HttpRoute}.
     *
     * @param route Route to create route sequence for.
     * @return New route sequence.
     */
    public HttpRouteSequence createRouteSequenceFor(final HttpRoute route) {
        final var routeFilters = filters.stream()
            .filter(filter -> HttpRoutables.intersect(filter, route))
            .toArray(HttpFilter[]::new);

        final var routeCatchers = catchers.stream()
            .filter(catcher -> HttpRoutables.intersect(catcher, route))
            .toArray(HttpCatcher[]::new);

        return new HttpRouteSequence(routeFilters, route, routeCatchers);
    }
}
