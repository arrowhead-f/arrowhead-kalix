package eu.arrowhead.kalix.internal.net.http.service;

import eu.arrowhead.kalix.net.http.service.HttpCatcher;
import eu.arrowhead.kalix.net.http.service.HttpRoute;
import eu.arrowhead.kalix.net.http.service.HttpValidator;
import eu.arrowhead.kalix.util.annotation.Internal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory class useful for constructing sound {@link HttpRouteSequence}
 * instances.
 */
@Internal
public class HttpRouteSequenceFactory {
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
    public HttpRouteSequenceFactory(final List<HttpCatcher<?>> catchers, final List<HttpValidator> validators) {
        this.catchers = new ArrayList<>(catchers);
        this.validators = new ArrayList<>(validators);

        this.catchers.sort(HttpRoutables::compareCatchers);
        this.validators.sort(HttpRoutables::compareValidators);
    }

    /**
     * Creates new {@link HttpRouteSequence} containing references to all
     * catchers and validators, known by this factory, that could, potentially,
     * match incoming  HTTP requests targeted at the given {@link HttpRoute}.
     *
     * @param route Route to create route sequence for.
     * @return New route sequence.
     */
    public HttpRouteSequence createRouteSequenceFor(final HttpRoute route) {
        final var routeValidators = validators.stream()
            .filter(validator -> HttpRoutables.intersect(validator, route))
            .toArray(HttpValidator[]::new);

        final var routeCatchers = catchers.stream()
            .filter(catcher -> HttpRoutables.intersect(catcher, route))
            .toArray(HttpCatcher[]::new);

        return new HttpRouteSequence(routeValidators, route, routeCatchers);
    }
}
