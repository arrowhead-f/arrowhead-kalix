package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.net.http.HttpHeaders;
import eu.arrowhead.kalix.net.http.HttpMethod;
import eu.arrowhead.kalix.net.http.HttpStatus;
import eu.arrowhead.kalix.net.http.HttpVersion;
import io.netty.handler.codec.DateFormatter;

import java.util.*;

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
     * @param method Kalix HTTP method.
     * @return Netty HTTP method.
     */
    public static io.netty.handler.codec.http.HttpMethod adapt(final HttpMethod method) {
        return io.netty.handler.codec.http.HttpMethod.valueOf(method.name());
    }

    /**
     * @param status Kalix HTTP status.
     * @return Netty HTTP status.
     */
    public static io.netty.handler.codec.http.HttpResponseStatus adapt(final HttpStatus status) {
        return io.netty.handler.codec.http.HttpResponseStatus.valueOf(status.code());
    }

    /**
     * @param headers Netty HTTP headers.
     * @return Kalix HTTP headers.
     */
    public static HttpHeaders adapt(final io.netty.handler.codec.http.HttpHeaders headers) {
        return new HttpHeaders() {
            @Override
            public Optional<String> get(final String name) {
                return Optional.ofNullable(headers.get(name));
            }

            @Override
            public List<String> getAll(final String name) {
                return headers.getAll(name);
            }

            @Override
            public Set<String> names() {
                return headers.names();
            }

            @Override
            public int size() {
                return headers.size();
            }

            @Override
            public HttpHeaders add(final String name, final String value) {
                headers.add(name, value);
                return this;
            }

            @Override
            public HttpHeaders addAll(final String name, final Iterable<String> values) {
                headers.add(name, values);
                return this;
            }

            @Override
            public HttpHeaders set(final String name, final String value) {
                headers.set(name, value);
                return this;
            }

            @Override
            public HttpHeaders setAll(final String name, final Iterable<String> values) {
                headers.set(name, values);
                return this;
            }

            @Override
            public HttpHeaders remove(final String name) {
                headers.remove(name);
                return this;
            }

            @Override
            public HttpHeaders clear() {
                headers.clear();
                return this;
            }

            @Override
            public Iterator<Map.Entry<String, String>> iterator() {
                return headers.iteratorAsString();
            }
        };
    }

    /**
     * @param headers Kalix HTTP headers.
     * @return Netty HTTP headers.
     */
    public static io.netty.handler.codec.http.HttpHeaders adapt(final HttpHeaders headers) {
        return new io.netty.handler.codec.http.HttpHeaders() {
            @Override
            public String get(final String name) {
                return headers.get(name)
                    .orElse(null);
            }

            @Override
            public Integer getInt(final CharSequence name) {
                return headers.get(name.toString())
                    .map(Integer::parseInt)
                    .orElse(null);
            }

            @Override
            public int getInt(final CharSequence name, final int defaultValue) {
                return headers.get(name.toString())
                    .map(Integer::parseInt)
                    .orElse(defaultValue);
            }

            @Override
            public Short getShort(final CharSequence name) {
                return headers.get(name.toString())
                    .map(Short::parseShort)
                    .orElse(null);
            }

            @Override
            public short getShort(final CharSequence name, final short defaultValue) {
                return headers.get(name.toString())
                    .map(Short::parseShort)
                    .orElse(defaultValue);
            }

            @Override
            public Long getTimeMillis(final CharSequence name) {
                return headers.get(name.toString())
                    .map(DateFormatter::parseHttpDate)
                    .map(Date::getTime)
                    .orElse(null);
            }

            @Override
            public long getTimeMillis(final CharSequence name, final long defaultValue) {
                return headers.get(name.toString())
                    .map(DateFormatter::parseHttpDate)
                    .map(Date::getTime)
                    .orElse(defaultValue);
            }

            @Override
            public List<String> getAll(final String name) {
                return headers.getAll(name);
            }

            @Override
            public List<Map.Entry<String, String>> entries() {
                final var list = new ArrayList<Map.Entry<String, String>>();
                iterator().forEachRemaining(list::add);
                return list;
            }

            @Override
            public boolean contains(final String name) {
                return headers.get(name)
                    .isPresent();
            }

            @Override
            public Iterator<Map.Entry<String, String>> iterator() {
                return headers.iterator();
            }

            @Override
            public Iterator<Map.Entry<CharSequence, CharSequence>> iteratorCharSequence() {
                final var iterator = headers.iterator();
                return new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Map.Entry<CharSequence, CharSequence> next() {
                        final var next = iterator.next();
                        return new Map.Entry<>() {
                            @Override
                            public CharSequence getKey() {
                                return next.getKey();
                            }

                            @Override
                            public CharSequence getValue() {
                                return next.getValue();
                            }

                            @Override
                            public CharSequence setValue(final CharSequence value) {
                                return next.setValue(value.toString());
                            }
                        };
                    }
                };
            }

            @Override
            public boolean isEmpty() {
                return iterator().hasNext();
            }

            @Override
            public int size() {
                return headers.size();
            }

            @Override
            public Set<String> names() {
                return headers.names();
            }

            @Override
            public io.netty.handler.codec.http.HttpHeaders add(final String name, final Object value) {
                headers.add(name, value.toString());
                return this;
            }

            @Override
            public io.netty.handler.codec.http.HttpHeaders add(final String name, final Iterable<?> values) {
                for (final var value : values) {
                    headers.add(name, value.toString());
                }
                return this;
            }

            @Override
            public io.netty.handler.codec.http.HttpHeaders addInt(final CharSequence name, final int value) {
                headers.add(name.toString(), Integer.toString(value));
                return this;
            }

            @Override
            public io.netty.handler.codec.http.HttpHeaders addShort(final CharSequence name, final short value) {
                headers.add(name.toString(), Short.toString(value));
                return this;
            }

            @Override
            public io.netty.handler.codec.http.HttpHeaders set(final String name, final Object value) {
                headers.set(name, value.toString());
                return this;
            }

            @Override
            public io.netty.handler.codec.http.HttpHeaders set(final String name, final Iterable<?> values) {
                headers.remove(name);
                return add(name, values);
            }

            @Override
            public io.netty.handler.codec.http.HttpHeaders setInt(final CharSequence name, final int value) {
                headers.set(name.toString(), Integer.toString(value));
                return this;
            }

            @Override
            public io.netty.handler.codec.http.HttpHeaders setShort(final CharSequence name, final short value) {
                headers.set(name.toString(), Short.toString(value));
                return this;
            }

            @Override
            public io.netty.handler.codec.http.HttpHeaders remove(final String name) {
                headers.remove(name);
                return this;
            }

            @Override
            public io.netty.handler.codec.http.HttpHeaders clear() {
                headers.clear();
                return this;
            }
        };
    }
}
