package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.net.http.HttpHeaders;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NettyHttpHeaders implements HttpHeaders {
    private final io.netty.handler.codec.http.HttpHeaders headers;

    public NettyHttpHeaders(final io.netty.handler.codec.http.HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public Optional<String> get(final CharSequence name) {
        return Optional.ofNullable(headers.get(name));
    }

    @Override
    public Optional<Integer> getAsInteger(final CharSequence name) {
        return Optional.ofNullable(headers.getInt(name));
    }

    @Override
    public List<String> getAll(final CharSequence name) {
        return headers.getAll(name);
    }

    @Override
    public HttpHeaders add(final CharSequence name, final CharSequence value) {
        headers.add(name, value);
        return this;
    }

    @Override
    public HttpHeaders add(final CharSequence name, final Iterable<String> values) {
        headers.add(name, values);
        return this;
    }

    @Override
    public HttpHeaders set(final CharSequence name, final CharSequence value) {
        headers.set(name, value);
        return this;
    }

    @Override
    public HttpHeaders set(final CharSequence name, final Iterable<String> values) {
        headers.set(name, values);
        return this;
    }

    @Override
    public HttpHeaders remove(final CharSequence name) {
        headers.remove(name);
        return this;
    }

    @Override
    public HttpHeaders clear() {
        headers.clear();
        return this;
    }

    @Override
    public Iterator<Map.Entry<CharSequence, CharSequence>> iterator() {
        return headers.iteratorCharSequence();
    }
}
