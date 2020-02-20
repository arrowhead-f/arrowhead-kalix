package eu.arrowhead.kalix.http.service;

import eu.arrowhead.kalix.util.concurrent.Future;
import eu.arrowhead.kalix.http.HttpVersion;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An incoming HTTP request, handled by a {@link HttpService}.
 */
public class HttpServiceRequest {
    /**
     * Requests the body of the request to be serialized into an instance of
     * given target class.
     * <p>
     * As the body may not have been fully received when this method is called,
     * a {@link Future} is returned whose
     * {@link Future#onResult(Future.Consumer)} method will be called when the
     * body is either ready or it is known that it cannot be received and/or
     * serialized.
     *
     * @param class_ Class to serialize request body into.
     * @param <C>    Type of {@code class_}.
     * @return Future of serialized request body.
     * @throws HttpServiceRequestException If the request body cannot be parsed.
     */
    public <C> Future<C> bodyAs(final Class<C> class_) throws HttpServiceRequestException {
        return null;
    }

    /**
     * Gets value of named header, if set.
     *
     * @param name Name of header. Case is ignored.
     * @return Header value, or {@code null}.
     */
    public Optional<String> header(final String name) {
        return null;
    }

    /**
     * @return Map of all request headers.
     */
    public Map<String, String> headers() {
        return null;
    }

    /**
     * Gets value of identified path parameter, if set.
     * <p>
     * This operation accesses an arbitrary list that has exactly the same size
     * as the number of path parameters of the {@link HttpPattern} matched
     * prior to this request becoming available. If an index is given outside
     * the bounds of this list, {@code null} is returned.
     * <p>
     * Note that it is possible to match a path parameter with an empty string.
     * It should never be assumed that a non-null value returned by this method
     * has a length larger than 0.
     *
     * @param index Position of path parameter in original pattern.
     * @return Path parameter value, if any.
     */
    public Optional<String> pathParameter(final int index) {
        return null;
    }

    /**
     * @return List of all path parameters.
     */
    public List<String> pathParameters() {
        return null;
    }

    /**
     * Gets value of named query parameter, if set.
     *
     * @param name Name of query parameter. Case sensitive.
     * @return Query parameter value, or {@code null}.
     */
    public Optional<String> queryParameter(final String name) {
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
