package eu.arrowhead.kalix.net;

/**
 * A handler meant to process requests matching some associated URI path.
 *
 * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC 3986, Section 3.3</a>
 */
@FunctionalInterface
public interface PathRoute<Request, Response> {
    /**
     * Called with an incoming request and a response handler.
     *
     * @param request  Information about the incoming HTTP request, including
     *                 its header and body.
     * @param response An object useful for indicating how and if the request
     *                 is to be responded to.
     * @return The body to use in the request response.
     * @throws Exception Whatever exception the handle may want to throw. The
     *                   exception should result in the request sender
     *                   receiving an error response message.
     */
    Object handle(Request request, Response response) throws Exception;
}