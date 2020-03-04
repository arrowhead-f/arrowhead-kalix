package eu.arrowhead.kalix.internal.net.http;

import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.*;
import java.util.function.Supplier;

/**
 * An unmodifiable map of query parameters that is lazily initialized when
 * first accessed.
 */
public class NettyQueryParameterMap implements Map<String, List<String>> {
    private final Supplier<Map<String, List<String>>> mapSupplier;

    private Map<String, List<String>> map;

    /**
     * @param decoder Netty query string decoder to use for loading parameters.
     */
    public NettyQueryParameterMap(final QueryStringDecoder decoder) {
        mapSupplier = decoder::parameters;
    }

    @Override
    public int size() {
        if (map == null) {
            map = mapSupplier.get();
        }
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        if (map == null) {
            map = mapSupplier.get();
        }
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        if (map == null) {
            map = mapSupplier.get();
        }
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        if (map == null) {
            map = mapSupplier.get();
        }
        return map.containsValue(value);
    }

    @Override
    public List<String> get(final Object key) {
        if (map == null) {
            map = mapSupplier.get();
        }
        final var value = map.get(key);
        if (value == null) {
            return null;
        }
        return Collections.unmodifiableList(value);
    }

    @Override
    public List<String> put(final String key, final List<String> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final Map<? extends String, ? extends List<String>> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        if (map == null) {
            map = mapSupplier.get();
        }
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public Collection<List<String>> values() {
        if (map == null) {
            map = mapSupplier.get();
        }
        return Collections.unmodifiableCollection(map.values());
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        if (map == null) {
            map = mapSupplier.get();
        }
        return Collections.unmodifiableSet(map.entrySet());
    }
}
