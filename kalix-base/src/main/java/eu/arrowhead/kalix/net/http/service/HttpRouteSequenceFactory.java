package eu.arrowhead.kalix.net.http.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory class useful for constructing sound {@link HttpRouteSequence}
 * instances.
 */
class HttpRouteSequenceFactory {
    private final List<HttpCatcher<?>> catchers;
    private final List<HttpValidator> validators;

    /**
     * Creates new factory containing given {@code catchers} and
     * {@code validators}.
     * <p>
     * Subsequently created {@link HttpRouteSequence}s will contain references
     * to all catchers and validators that could, potentially, match incoming
     * HTTP requests targeted at certain {@link HttpRoute}s.
     *
     * @param catchers   Route exception handlers.
     * @param validators Route validation handlers.
     */
    HttpRouteSequenceFactory(final List<HttpCatcher<?>> catchers, final List<HttpValidator> validators) {
        this.catchers = catchers;
        this.validators = validators;

        this.catchers.sort(HttpCatcher::compareTo);
        this.validators.sort(HttpValidator::compareTo);
    }

    /**
     * Creates new {@link HttpRouteSequence} containing references to all
     * catchers and validators, known by this factory, that could, potentially,
     * match incoming  HTTP requests targeted at the given {@link HttpRoute}.
     *
     * @param route Route to create route sequence for.
     * @return New route sequence.
     */
    HttpRouteSequence createRouteSequenceFor(final HttpRoute route) {
        final var routeValidators = validators.stream()
            .filter(validator -> validator.matchesIntersectionOf(route))
            .collect(Collectors.toList());

        final var routeCatchers = catchers.stream()
            .filter(catcher -> catcher.matchesIntersectionOf(route))
            .collect(Collectors.toList());

        return new HttpRouteSequence(routeValidators, route, routeCatchers);
    }
}
