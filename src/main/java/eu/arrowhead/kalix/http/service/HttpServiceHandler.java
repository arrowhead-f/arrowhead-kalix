package eu.arrowhead.kalix.http.service;

/**
 * A service handler, meant to process incoming HTTP requests matching some
 * arbitrary set of preconditions.
 */
@FunctionalInterface
public interface HttpServiceHandler {
    /**
     * Called with an incoming HTTP request and a response handler.
     *
     * @param request  Information about the incoming HTTP request, including
     *                 its header and body.
     * @param response An object useful for indicating how and if the request
     *                 is to be responded to.
     * @return The body to use in the request response. The object can be
     * expected to be serialized automatically into the appropriate encoding.
     * If a {@link eu.arrowhead.kalix.concurrent.Future} is returned, it can be
     * expected to be awaited before its contents are serialized and returned
     * to the caller. If {@code null} is returned or {@link Void} is used as
     * type parameter to a returned
     * {@link eu.arrowhead.kalix.concurrent.Future}, assembling a response is
     * either delegated to some other handler or used to indicate that no
     * response is to be sent.
     * @throws Exception Whatever exception the handle may want to throw. If
     *                   the HTTP service owning this handle knows how to
     *                   translate the exception into a certain kind of HTTP
     *                   response, it should. Otherwise the requester should
     *                   receive a 500 Internal Server Error response without
     *                   any details and the exception be logged (if logging is
     *                   enabled).
     */
    Object handle(HttpServiceRequest request, HttpServiceResponse response) throws Exception;
}
