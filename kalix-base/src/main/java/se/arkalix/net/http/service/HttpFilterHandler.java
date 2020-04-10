package se.arkalix.net.http.service;

import se.arkalix.util.concurrent.Future;

/**
 * A route filter, meant to validate or handle incoming HTTP requests
 * matching some {@link HttpFilter set of preconditions}.
 */
@FunctionalInterface
public interface HttpFilterHandler {
    /**
     * Called with an incoming HTTP request and a response handler.
     *
     * @param request  Information about the incoming HTTP request.
     * @param response An object useful for indicating how the request is to be
     *                 responded to. If a status code is set by this handler,
     *                 the provided request is responded to as soon as this
     *                 handler completes, which implies that no more filters
     *                 or any route is invoked with the request.
     * @return Future completed when filtering is complete.
     * @throws Exception The exception should be passed on to any matching
     *                   {@link HttpCatcher}.
     */
    Future<?> handle(HttpServiceRequest request, HttpServiceResponse response) throws Exception;
}