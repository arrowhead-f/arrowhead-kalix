package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.util.concurrent.Future;

/**
 * A route handler, meant to process incoming HTTP requests matching some
 * arbitrary set of preconditions.
 */
@FunctionalInterface
public interface HttpValidatorHandler {
    /**
     * Called with an incoming HTTP request and a response handler.
     *
     * @param request  Information about the incoming HTTP request.
     * @param response An object useful for indicating how the request is to be
     *                 responded to. If a status code is set by this handler,
     *                 the provided request is responded to as soon as this
     *                 handler completes, which implies that no more validators
     *                 or any route is invoked with the request.
     * @return Future completed when validation is complete.
     * @throws Exception The exception should be passed on to any matching
     *                   {@link HttpCatcher}.
     */
    Future<?> handle(HttpServiceRequest request, HttpServiceResponse response) throws Exception;
}