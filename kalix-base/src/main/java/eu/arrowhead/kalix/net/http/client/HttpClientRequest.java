package eu.arrowhead.kalix.net.http.client;

import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpVersion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An outgoing HTTP request.
 */
public class HttpClientRequest {
    private HttpVersion version;
    private Map<String, List<String>> queryParameters = new HashMap<>();
    private Object body;

    /**
     * Creates new HTTP/1.1 request.
     */
    public HttpClientRequest() {
        this(HttpVersion.HTTP_11);
    }

    /**
     * Creates new HTTP request with the specified version indicator.
     *
     * @param version HTTP version to use.
     */
    public HttpClientRequest(final HttpVersion version) {
        this.version = version;
    }

    /**
     * @return Previously set request body, if any.
     */
    public Optional<Object> body() {
        return Optional.ofNullable(body);
    }

    /**
     * Sets request body.
     * <p>
     * The body is later encoded using the encoding associated with the media
     * type stated in the "content-type" header in this request, if that
     * encoding is supported.
     *
     * @param body Desired request body.
     * @return This request.
     */
    public HttpClientRequest body(final Object body) {
        this.body = body;
        return this;
    }

    /**
     * Gets value of named header, if set.
     *
     * @param name Name of header. Case is ignored. Prefer lowercase.
     * @return Header value, or {@code null}.
     */
    public Optional<String> header(final String name) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets header with {@code name} to given value.
     *
     * @param name  Name of header. Case is ignored. Prefer lowercase.
     * @param value Desired header value.
     * @return This request.
     */
    public HttpClientRequest header(final String name, final String value) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return Case-insensitive map of request headers. Prefer lowercase keys.
     */
    public HttpHeaders headers() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets value of previously set query parameter, if any.
     *
     * @param name Name of query parameter. Case sensitive.
     * @return Query parameter value, or {@code null}.
     */
    public Optional<String> queryParameter(final String name) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets query parameter with {@code name} to given value.
     *
     * @param name  Name of query parameter. Case sensitive.
     * @param value Query parameter value. Case sensitive.
     * @return This request.
     */
    public HttpClientRequest queryParameter(final String name, final String value) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return Map of all query parameters.
     */
    public Map<String, String> queryParameters() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return HTTP version used by request.
     */
    public HttpVersion version() {
        return version;
    }

    /**
     * Sets HTTP version to use with request.
     *
     * @param version Target HTTP version.
     * @return This request.
     */
    public HttpClientRequest version(final HttpVersion version) {
        this.version = version;
        return this;
    }
}
