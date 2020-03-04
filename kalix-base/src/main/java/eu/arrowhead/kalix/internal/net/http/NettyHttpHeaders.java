package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.net.http.HttpHeaders;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NettyHttpHeaders extends HttpHeaders {
    private final io.netty.handler.codec.http.HttpHeaders headers;

    public NettyHttpHeaders(final io.netty.handler.codec.http.HttpHeaders headers) {
        super();
        this.headers = headers;
    }

    @Override
    public HttpHeaders add(final String name, final String value) {
        headers.add(name, value);
        return this;
    }

    @Override
    public Optional<String> get(final String name) {
        return Optional.ofNullable(headers.get(name));
    }

    @Override
    public List<String> getAll(final String name) {
        return headers.getAll(name);
    }

    @Override
    public HttpHeaders set(final String name, final String value) {
        headers.set(name, value);
        return this;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return headers.iteratorAsString();
    }
}
