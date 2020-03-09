package eu.arrowhead.kalix.net.http.client;

import eu.arrowhead.kalix.dto.DataWritable;
import eu.arrowhead.kalix.internal.net.http.NettyHttpHeaders;
import eu.arrowhead.kalix.net.http.HttpBodySender;
import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpVersion;
import io.netty.handler.codec.http.DefaultHttpHeaders;

import java.nio.file.Path;
import java.util.*;

/**
 * An outgoing HTTP request.
 */
public class HttpClientRequest implements HttpBodySender<HttpClientRequest> {
    private final NettyHttpHeaders headers = new NettyHttpHeaders(new DefaultHttpHeaders());
    private final Map<String, List<String>> queryParameters = new HashMap<>();

    private Object body = null;
    private HttpVersion version;

    @Override
    public Optional<Object> body() {
        return Optional.ofNullable(body);
    }

    @Override
    public HttpClientRequest body(final byte[] byteArray) {
        body = byteArray;
        return this;
    }

    @Override
    public HttpClientRequest body(final DataWritable dto) {
        body = dto;
        return this;
    }

    @Override
    public HttpClientRequest body(final Path path) {
        body = path;
        return this;
    }

    @Override
    public HttpClientRequest body(final String string) {
        body = string;
        return this;
    }

    @Override
    public HttpClientRequest clearBody() {
        body = null;
        return this;
    }

    /**
     * Gets value of first header with given {@code name}, if any such.
     *
     * @param name Name of header. Case is ignored. Prefer lowercase.
     * @return Header value, or {@code null}.
     */
    public Optional<String> header(final CharSequence name) {
        return headers.get(name);
    }

    /**
     * Sets header with {@code name} to given value.
     *
     * @param name  Name of header. Case is ignored. Prefer lowercase.
     * @param value Desired header value.
     * @return This request.
     */
    public HttpClientRequest header(final CharSequence name, final CharSequence value) {
        headers.set(name, value);
        return this;
    }

    /**
     * Gets all header values associated with given {@code name}, if any.
     *
     * @param name Name of header. Case is ignored. Prefer lowercase.
     * @return Header values. May be an empty list.
     */
    public List<String> headers(final CharSequence name) {
        return headers.getAll(name);
    }

    /**
     * @return <i>Modifiable</i> map of all request headers.
     */
    public HttpHeaders headers() {
        return headers;
    }

    /**
     * Gets first query parameter with given name, if any such.
     *
     * @param name Name of query parameter. Case sensitive.
     * @return Query parameter value, if a corresponding parameter name exists.
     */
    public Optional<String> queryParameter(final String name) {
        final var values = queryParameters.get(name);
        return Optional.ofNullable(values.size() > 0 ? values.get(0) : null);
    }

    /**
     * Sets query parameter pair, replacing all previous such with the same
     * name.
     *
     * @param name  Name of query parameter. Case sensitive.
     * @param value Desired parameter value.
     * @return Query parameter value, if a corresponding parameter name exists.
     */
    public HttpClientRequest queryParameter(final String name, final CharSequence value) {
        final var list = new ArrayList<String>(1);
        list.add(value.toString());
        queryParameters.put(name, list);
        return this;
    }

    /**
     * @return Modifiable map of query parameters.
     */
    public Map<String, List<String>> queryParameters() {
        return queryParameters;
    }

    /**
     * @return HTTP version used by request.
     */
    public HttpVersion version() {
        return version;
    }

    public HttpClientRequest version(final HttpVersion version) {
        this.version = version;
        return this;
    }
}
