package eu.arrowhead.kalix.http;

/**
 * A handler meant to process HTTP requests matching some associated URI path.
 *
 * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986, Section 3.3</a>
 */
@FunctionalInterface
public interface HttpHandler {
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
     * to the caller.
     * @throws Exception Whatever exception the handle may want to throw. If
     *                   the HTTP service owning this handle knows how to
     *                   translate the exception into a certain kind of HTTP
     *                   response, it should. Otherwise the requester should
     *                   receive a 500 Internal Server Error response without
     *                   any details.
     */
    Object handle(HttpRequest request, HttpResponse response) throws Exception;
}
