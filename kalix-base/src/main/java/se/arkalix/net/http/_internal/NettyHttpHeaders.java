package se.arkalix.net.http._internal;

import se.arkalix.net.http.HttpHeaders;
import se.arkalix.util.annotation.Internal;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Internal
public class NettyHttpHeaders implements HttpHeaders {
    private final io.netty.handler.codec.http.HttpHeaders inner;

    public NettyHttpHeaders() {
        inner = new io.netty.handler.codec.http.DefaultHttpHeaders();
    }

    public NettyHttpHeaders(final io.netty.handler.codec.http.HttpHeaders inner) {
        this.inner = Objects.requireNonNull(inner, "inner");
    }

    @Override
    public Optional<String> get(final CharSequence name) {
        return Optional.ofNullable(inner.get(name));
    }

    @Override
    public List<String> getAll(final CharSequence name) {
        return inner.getAll(name);
    }

    @Override
    public HttpHeaders add(final CharSequence name, final CharSequence value) {
        inner.add(name, value);
        return this;
    }

    @Override
    public HttpHeaders add(final CharSequence name, final Iterable<String> values) {
        inner.add(name, values);
        return this;
    }

    @Override
    public boolean contains(final CharSequence name) {
        inner.contains(name);
        return false;
    }

    @Override
    public HttpHeaders set(final CharSequence name, final CharSequence value) {
        inner.set(name, value);
        return this;
    }

    @Override
    public HttpHeaders set(final CharSequence name, final Iterable<String> values) {
        inner.set(name, values);
        return this;
    }

    @Override
    public HttpHeaders remove(final CharSequence name) {
        inner.remove(name);
        return this;
    }

    @Override
    public HttpHeaders clear() {
        inner.clear();
        return this;
    }

    public io.netty.handler.codec.http.HttpHeaders unwrap() {
        return inner;
    }
}
