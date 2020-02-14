package eu.arrowhead.kalix.http;

/**
 * Represents an HTTP route handler, receiving incoming requests matching a
 * certain path and/or some other requirements.
 */
@FunctionalInterface
public interface HttpRoute {
    /**
     * Called with an incoming HTTP request.
     *
     * @param request  Information about the incoming HTTP request, including
     *                 its header and body.
     * @param response An object useful for indicating how and if the request
     *                 is to be responded to.
     * @return The body to use in the request response. The object will be
     * serialized into whatever encoding is supported by the handler service
     * and preferred by the request sender. If a
     * {@link eu.arrowhead.kalix.concurrent.Future} is returned, it is awaited
     * before its contents are serialized and returned to the caller.
     * @throws Exception Whatever exception the handle may want to throw. If
     *                   the HTTP service owning this handle knows how to
     *                   translate the exception into a certain kind of HTTP
     *                   response, it will. Otherwise the requester receives a
     *                   500 Internal Server Error response without any details.
     */
    Object handle(HttpRequest request, HttpResponse response) throws Exception;
}
