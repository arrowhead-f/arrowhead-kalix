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
     * @param request  Information about the incoming HTTP request, excluding
     *                 its body.
     * @param response An object useful for indicating how the request is to be
     *                 responded to. If a status code or body is set by this
     *                 handler, the provided request is responded to
     *                 immediately, which implies that no more validators or
     *                 any route is invoked with the request.
     * @return Future completed when validation is complete.
     * @throws Exception Whatever exception the handle may want to throw. If
     *                   the HTTP service owning this handle knows how to
     *                   translate the exception into a certain kind of HTTP
     *                   response, it should. Otherwise the requester should
     *                   receive a 500 Internal Server Error response without
     *                   any details and the exception be logged (if logging is
     *                   enabled).
     */
    Future<?> handle(HttpServiceRequestHead request, HttpServiceResponse response) throws Exception;
}