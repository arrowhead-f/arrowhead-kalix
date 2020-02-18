package eu.arrowhead.kalix.http;

import eu.arrowhead.kalix.concurrent.Future;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A fully received HTTP request.
 */
public class HttpRequest {
    /**
     * Requests the body of the request to be serialized into an instance of
     * given target class.
     * <p>
     * As the body may not have been fully received when this method is called,
     * a {@link Future} is returned whose {@link Future#onDone(Supplier)}
     * method will be called when the body is either ready or it is known that
     * it cannot be received and/or serialized.
     *
     * @param class_ Class to serialize request body into.
     * @param <C>    Type of {@code class_}.
     * @return Future of serialized request body.
     * @throws HttpRequestException If the request body cannot be parsed.
     */
    public <C> Future<C> bodyAs(final Class<C> class_) throws HttpRequestException {
        return null;
    }

    /**
     * Gets value of named header, or {@code null} if the header is not set.
     *
     * @param name Name of header. Case is ignored.
     * @return Header value, or {@code null}.
     */
    public String header(final String name) {
        return null;
    }

    /**
     * @return Map of all request headers.
     */
    public Map<String, String> headers() {
        return null;
    }

    /**
     * Gets value of identified path parameter, or {@code null}.
     * <p>
     * This operation accesses an arbitrary list that has exactly the same size
     * as the number of path parameters in some original {@link HttpPattern}.
     * If an index is given outside the bounds of this list, {@code null} is
     * returned.
     * <p>
     * Note that it is possible to match a path parameter with an empty string.
     * It should never be assumed that a non-null value returned by this method
     * has a length larger than 0.
     *
     * @param index Position of path parameter in original pattern.
     * @return Path parameter value, or {@code null}.
     */
    public String pathParameter(final int index) {
        return null;
    }

    /**
     * @return Map of all path parameters.
     */
    public List<String> pathParameters() {
        return null;
    }

    /**
     * Gets value of named query parameter, or {@code null} if the query
     * parameter is not set.
     *
     * @param name Name of query parameter. Case is ignored.
     * @return Query parameter value, or {@code null}.
     */
    public String queryParameter(final String name) {
        return null;
    }

    /**
     * @return Map of all query parameters.
     */
    public Map<String, String> queryParameters() {
        return null;
    }

    /**
     * @return Information about the request sender.
     */
    public HttpRequester requester() {
        return null;
    }

    /**
     * @return HTTP version used by request.
     */
    public HttpVersion version() {
        return null;
    }
}
