package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.util.concurrent.Future;

/**
 * A handler meant to process exceptions occurring while handling incoming
 * HTTP requests, or while responding to them.
 */
@FunctionalInterface
public interface HttpCatcherHandler<T extends Throwable> {
    /**
     * Called with a an exception, an incoming HTTP request and an HTTP
     * response handler.
     *
     * @param throwable Exception causing this handler to be invoked.
     * @param request   Information about the incoming HTTP request.
     * @param response  An object useful for indicating how the request is to
     *                  be responded to. If a status code is set by this
     *                  handler, the provided request is responded to as soon
     *                  as this handler completes, which implies the exception
     *                  has been handled and that no more catchers are to be
     *                  invoked with the request.
     * @return Future completed when catching is complete.
     * @throws Exception The exception should be passed on to the next matching
     *                   catcher, if any.
     */
    Future<?> handle(T throwable, HttpServiceRequest request, HttpServiceResponse response) throws Exception;
}
