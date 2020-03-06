package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.HttpVersion;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Various HTTP-related utilities related to Netty.
 */
public class NettyHttp {
    private NettyHttp() {}

    /**
     * @param version Netty HTTP version.
     * @return Kalix HTTP version.
     */
    public static HttpVersion adapt(final io.netty.handler.codec.http.HttpVersion version) {
        return HttpVersion.getOrCreate(version.majorVersion(), version.minorVersion());
    }

    /**
     * @param method Netty HTTP method.
     * @return Kalix HTTP method.
     */
    public static HttpMethod adapt(final io.netty.handler.codec.http.HttpMethod method) {
        return HttpMethod.valueOf(method.name());
    }

    /**
     * @param headers Netty HTTP headers.
     * @return Kalix HTTP headers.
     */
    public static HttpHeaders adapt(final io.netty.handler.codec.http.HttpHeaders headers) {
        return new HttpHeaders() {
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
        };
    }
}
