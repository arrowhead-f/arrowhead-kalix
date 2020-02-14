package eu.arrowhead.kalix.http;

import java.util.Map;

/**
 * A fully received HTTP request.
 */
public class HttpRequest {
    /**
     * Requests the body of the request to be serialized into an instance of
     * given target class.
     *
     * @param class_ Desired class type to serialize request body into.
     * @param <C>    Type annotation for desired class type.
     * @return Serialized request body.
     * @throws HttpRequestException If the request body cannot be parsed.
     */
    public <C> C getBodyAs(final Class<C> class_) throws HttpRequestException {
        return null;
    }

    /**
     * Gets value of named header, or {@code null} if the header is not set.
     *
     * @param name Name of header. Case is ignored.
     * @return Header value, or {@code null}.
     */
    public String getHeader(final String name) {
        return null;
    }

    /**
     * @return Map of all request headers.
     */
    public Map<String, String> getHeaders() {
        return null;
    }

    /**
     * Gets value of named path parameter, or {@code null} if the path parameter
     * is not set.
     *
     * @param name Name of path parameter. Case is ignored.
     * @return Path parameter value, or {@code null}.
     */
    public String getPathParameter(final String name) {
        return null;
    }

    /**
     * @return Map of all path parameters.
     */
    public Map<String, String> getPathParameters() {
        return null;
    }

    /**
     * Gets value of named query parameter, or {@code null} if the query
     * parameter is not set.
     *
     * @param name Name of query parameter. Case is ignored.
     * @return Query parameter value, or {@code null}.
     */
    public String getQueryParameter(final String name) {
        return null;
    }

    /**
     * @return Map of all query parameters.
     */
    public Map<String, String> getQueryParameters() {
        return null;
    }

    /**
     * @return Information about the request sender.
     */
    public HttpRequester getRequester() {
        return null;
    }

    /**
     * @return HTTP version used by request.
     */
    public HttpVersion getVersion() {
        return null;
    }
}
