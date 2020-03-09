package eu.arrowhead.kalix.internal.net.http.service;

import eu.arrowhead.kalix.net.http.service.HttpCatcher;
import eu.arrowhead.kalix.net.http.service.HttpRoute;
import eu.arrowhead.kalix.net.http.service.HttpRouteTask;
import eu.arrowhead.kalix.net.http.service.HttpValidator;
import eu.arrowhead.kalix.util.annotation.Internal;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.util.ArrayList;

/**
 * A sequence of {@link HttpValidator}s, a {@link HttpRoute} and
 * {@link HttpCatcher}s, together able to handle the validation, processing and
 * error handling related to certain incoming HTTP requests, respectively.
 */
@Internal
public class HttpRouteSequence {
    private final HttpValidator[] validators;
    private final HttpRoute route;
    private final HttpCatcher<?>[] catchers;

    /**
     * Creates new route sequence from given components.
     * <p>
     * Note that for the validators and catchers to ever be invoked, there has
     * to exist method/path pairs that can be successfully matched against
     * both each of them and the provided route.
     *
     * @param validators Validators to include in route sequence.
     * @param route      Target route.
     * @param catchers   Catchers to include in route sequence.
     */
    public HttpRouteSequence(
        final HttpValidator[] validators,
        final HttpRoute route,
        final HttpCatcher<?>[] catchers)
    {
        this.validators = validators;
        this.route = route;
        this.catchers = catchers;
    }

    /**
     * Offers this route sequence the opportunity to handle given request.
     *
     * @param task Incoming HTTP request route task.
     * @return Future completed with {@code true} only if given request was
     * handled.
     */
    public Future<Boolean> tryHandle(final HttpRouteTask task) {
        final var pathParameters = new ArrayList<String>(route.pattern().map(HttpPattern::nParameters).orElse(0));
        if (!route.match(task, pathParameters)) {
            return Future.success(false);
        }
        return Future
            .flatReducePlain(validators, false, (isHandled, validator) -> {
                if (isHandled) {
                    return Future.success(true);
                }
                return validator.tryHandle(task);
            })
            .flatMap(isHandled -> {
                if (isHandled) {
                    return Future.success(true);
                }
                return route
                    .handle(task.request().newWithPathParameters(pathParameters), task.response())
                    .map(ignored -> true);
            })
            .flatMapCatch(throwable -> Future.flatReducePlain(catchers, false, (isHandled, catcher) -> {
                if (isHandled) {
                    return Future.success(true);
                }
                return catcher.tryHandle(throwable, task);
            }));
    }
}
