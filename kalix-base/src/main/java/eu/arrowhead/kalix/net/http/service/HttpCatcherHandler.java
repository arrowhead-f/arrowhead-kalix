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
     * @param request   Information about the incoming HTTP request, excluding
     *                  its body.
     * @param response  An object useful for indicating how the request is to
     *                  be responded to.
     * @return Future completed when catching is complete. Its value should be
     * {@code true} only if the throwable was handled.
     * @throws Exception Whatever exception the handle may want to throw. If
     *                   the HTTP service owning this handle knows how to
     *                   translate the exception into a certain kind of HTTP
     *                   response, it should. Otherwise the requester should
     *                   receive a 500 Internal Server Error response without
     *                   any details and the exception be logged (if logging is
     *                   enabled).
     */
    Future<Boolean> handle(T throwable, HttpServiceRequestHead request, HttpServiceResponse response) throws Exception;
}
