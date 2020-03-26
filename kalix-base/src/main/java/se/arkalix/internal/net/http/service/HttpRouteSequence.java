package se.arkalix.internal.net.http.service;

import se.arkalix.net.http.service.*;
import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import java.util.ArrayList;
import java.util.Objects;

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
        return tryValidators(task, 0)
            .flatMap(isHandled -> {
                if (isHandled) {
                    return Future.success(true);
                }
                final var response = task.response();
                return route
                    .handle(task.request().cloneAndSet(pathParameters), response)
                    .mapResult(ignored -> {
                        if (response.status().isEmpty()) {
                            return Result.failure(new IllegalStateException("" +
                                "HTTP route " +
                                route.method()
                                    .map(Object::toString)
                                    .orElse("<?>") +
                                ' ' +
                                task.basePath() +
                                route.pattern()
                                    .map(Object::toString)
                                    .orElse("") +
                                " never set a status code; a status " +
                                " must be set"));
                        }
                        return Result.success(true);
                    });
            })
            .flatMapCatch(Throwable.class, throwable -> tryCatchers(throwable, task, 0));
    }

    private Future<Boolean> tryValidators(final HttpRouteTask task, final int index) {
        if (index >= validators.length) {
            return Future.success(false);
        }
        final var validator = validators[index];
        return validator.tryHandle(task)
            .flatMap(isHandled -> {
                if (isHandled) {
                    return Future.success(true);
                }
                return tryValidators(task, index + 1);
            });
    }

    private Future<Boolean> tryCatchers(final Throwable throwable, final HttpRouteTask task, final int index) {
        if (index >= catchers.length) {
            return Future.failure(throwable);
        }
        final var catcher = catchers[index];
        return catcher.tryHandle(throwable, task)
            .flatMap(isHandled -> {
                if (isHandled) {
                    return Future.success(true);
                }
                return tryCatchers(throwable, task, index + 1);
            });
    }
}
