package eu.arrowhead.kalix.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A {@link HashMap} whose {@link String} keys are guaranteed to consist of
 * only lowercase characters.
 *
 * @param <V> Type of map values.
 */
public class LowercaseHashMap<V> implements Map<String, V> {
    private final HashMap<String, V> inner;

    /**
     * Creates empty {@code LowercaseMap} with the specified initial
     * capacity and load factor.
     *
     * @param initialCapacity Initial capacity.
     * @param loadFactor      Load factor.
     * @throws IllegalArgumentException If the initial capacity is negative
     *                                  or the load factor is below 1.
     */
    public LowercaseHashMap(int initialCapacity, float loadFactor) {
        inner = new HashMap<>(initialCapacity, loadFactor);
    }

    /**
     * Creates empty {@code LowercaseMap} with the specified initial
     * capacity and the default load factor (0.75).
     *
     * @param initialCapacity the initial capacity.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public LowercaseHashMap(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    /**
     * Creates empty {@code LowercaseMap} with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public LowercaseHashMap() {
        this(16);
    }

    private String convertKey(final String key) {
        return key.toLowerCase();
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return inner.containsKey(convertKey((String) key));
    }

    @Override
    public boolean containsValue(final Object value) {
        return inner.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return inner.get(convertKey((String) key));
    }

    @Override
    public V put(String key, V value) {
        return inner.put(convertKey(key), value);
    }

    @Override
    public V remove(Object key) {
        return inner.remove(convertKey((String) key));
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        m.forEach((key, value) -> inner.put(convertKey(key), value));
    }

    @Override
    public void clear() {
        inner.clear();
    }

    @Override
    public Set<String> keySet() {
        return inner.keySet();
    }

    @Override
    public Collection<V> values() {
        return inner.values();
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        return inner.entrySet();
    }
}
