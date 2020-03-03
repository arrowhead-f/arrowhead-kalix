package eu.arrowhead.kalix.net.http.service;

import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpVersion;
import eu.arrowhead.kalix.util.concurrent.Future;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * An incoming HTTP request, handled by a {@link HttpService}.
 */
public class HttpServiceRequestHead {
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
    public HttpHeaders headers() {
        return null;
    }

    /**
     * @return Request URL path. Will always start with a leading forward
     * slash ({@code /}).
     */
    public String path() {
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
