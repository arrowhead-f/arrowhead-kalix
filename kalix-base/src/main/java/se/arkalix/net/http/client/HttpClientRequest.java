package se.arkalix.net.http.client;

import se.arkalix.internal.net.DefaultMessageOutgoing;
import se.arkalix.internal.net.http.HttpPaths;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.HttpOutgoingRequest;
import se.arkalix.net.http.HttpVersion;

import java.util.*;

/**
 * An HTTP request that can be sent to an HTTP server via an {@link HttpClient}.
 */
public class HttpClientRequest
    extends DefaultMessageOutgoing<HttpClientRequest>
    implements HttpOutgoingRequest<HttpClientRequest>
{
    private final HttpHeaders headers = new HttpHeaders();
    private final Map<String, List<String>> queryParameters = new HashMap<>();

    private HttpMethod method = null;
    private String path = null;
    private HttpVersion version = null;

    @Override
    public Optional<HttpMethod> method() {
        return Optional.ofNullable(method);
    }

    @Override
    public HttpClientRequest method(final HttpMethod method) {
        this.method = method;
        return this;
    }

    @Override
    public Optional<String> path() {
        return Optional.ofNullable(path);
    }

    @Override
    public HttpClientRequest path(final String path) {
        if (path != null && !HttpPaths.isValidPath(path)) {
            throw new IllegalArgumentException("Invalid RFC3986 path: " + path);
        }
        this.path = path;
        return this;
    }

    @Override
    public HttpClientRequest queryParameter(final String name, final Object value) {
        final var list = new ArrayList<String>(1);
        list.add(value.toString());
        queryParameters.put(name, list);
        return this;
    }

    @Override
    public Map<String, List<String>> queryParameters() {
        return queryParameters;
    }

    @Override
    public Optional<HttpVersion> version() {
        return Optional.ofNullable(version);
    }

    @Override
    public HttpClientRequest version(final HttpVersion version) {
        this.version = version;
        return this;
    }

    @Override
    public HttpClientRequest header(final CharSequence name, final CharSequence value) {
        headers.set(name, value);
        return this;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public HttpClientRequest clearHeaders() {
        headers.clear();
        return this;
    }

    @Override
    protected HttpClientRequest self() {
        return this;
    }
}
