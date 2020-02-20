package eu.arrowhead.kalix.http.client;

import eu.arrowhead.kalix.util.LowercaseHashMap;
import eu.arrowhead.kalix.http.HttpVersion;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * An outgoing HTTP request.
 */
public class HttpClientRequest {
    private HttpVersion version;
    private Map<String, String> queryParameters = new HashMap<>(4);
    private Map<String, String> headers = new LowercaseHashMap<>();
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
     * The body is serialized into the format specified by the "content-type"
     * header in the this request when provided to a {@link HttpClient}.
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
        return Optional.ofNullable(headers.get(name));
    }

    /**
     * Sets header with {@code name} to given value.
     *
     * @param name  Name of header. Case is ignored. Prefer lowercase.
     * @param value Desired header value.
     * @return This request.
     */
    public HttpClientRequest header(final String name, final String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * @return Case-insensitive map of request headers. Prefer lowercase keys.
     */
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * Replaces all existing request headers.
     * <p>
     * If {@code headers} contains keys that would be identical if compared
     * without case sensitivity, it is undefined which of their values ends up
     * in this request.
     *
     * @param headers New map of response headers.
     * @return This response object.
     */
    public HttpClientRequest headers(final Map<String, String> headers) {
        if (headers instanceof LowercaseHashMap) {
            this.headers = headers;
        }
        else {
            this.headers.clear();
            this.headers.putAll(headers);
        }
        return this;
    }

    /**
     * Gets value of previously set query parameter, if any.
     *
     * @param name Name of query parameter. Case sensitive.
     * @return Query parameter value, or {@code null}.
     */
    public Optional<String> queryParameter(final String name) {
        return Optional.ofNullable(queryParameters.get(name));
    }

    /**
     * Sets query parameter with {@code name} to given value.
     *
     * @param name  Name of query parameter. Case sensitive.
     * @param value Query parameter value. Case sensitive.
     * @return This request.
     */
    public HttpClientRequest queryParameter(final String name, final String value) {
        queryParameters.put(name, value);
        return this;
    }

    /**
     * @return Map of all query parameters.
     */
    public Map<String, String> queryParameters() {
        return queryParameters;
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
