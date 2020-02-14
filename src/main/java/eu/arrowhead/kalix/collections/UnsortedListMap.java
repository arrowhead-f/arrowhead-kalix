package eu.arrowhead.kalix.collections;

import java.util.*;

/**
 * A {@link Map} optimized for small collections (e.g. less than 20 entries).
 * <p>
 * Internally, an unsorted list of entries are maintain, which are scanned
 * linearly every time a new entry is added, removed or requested.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class UnsortedListMap<K, V> implements Map<K, V> {
    private final ArrayList<Map.Entry<K, V>> entries;

    /**
     * Creates new empty {@link UnsortedListSet} with {@code 0} capacity.
     * <p>
     * An internal buffer is allocated only when data is first inserted into
     * the set.
     */
    public UnsortedListMap() {
        this(0);
    }

    private UnsortedListMap(final int capacity) {
        entries = new ArrayList<>(capacity);
    }

    /**
     * Creates new {@link UnsortedListMap} with specified minimum capacity.
     *
     * @param capacity Number of entries to allocate memory for.
     * @param <K>      Key type.
     * @param <V>      Value type.
     * @return New {@link UnsortedListMap}.
     */
    public static <K, V> UnsortedListMap<K, V> withCapacity(final int capacity) {
        return new UnsortedListMap<>(capacity);
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        for (final var entry : entries) {
            if (Objects.equals(entry.getKey(), key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(final Object value) {
        for (final var entry : entries) {
            if (Objects.equals(entry.getValue(), value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(final Object key) {
        for (final var entry : entries) {
            if (Objects.equals(entry.getKey(), key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public V put(final K key, final V value) {
        for (final var entry : entries) {
            if (Objects.equals(entry.getKey(), key)) {
                return entry.setValue(value);
            }
        }
        entries.add(new Entry<>(key, value));
        return null;
    }

    @Override
    public V remove(final Object key) {
        final int size = entries.size();
        for (var i = 0; i < size; ++i) {
            final var entry = entries.get(i);
            if (Objects.equals(entry.getKey(), key)) {
                return entries.remove(i).getValue();
            }
        }
        return null;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public Set<K> keySet() {
        final var keys = new ArrayList<K>(entries.size());
        for (final var entry : entries) {
            keys.add(entry.getKey());
        }
        return UnsortedListSet.fromListUnchecked(keys);
    }

    @Override
    public Collection<V> values() {
        final var values = new ArrayList<V>(entries.size());
        for (final var entry : entries) {
            values.add(entry.getValue());
        }
        return UnsortedListSet.fromListUnchecked(values);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return UnsortedListSet.fromListUnchecked(entries);
    }

    static final class Entry<K, V> implements Map.Entry<K, V> {
        K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Entry<?, ?> entry = (Entry<?, ?>) o;
            return key.equals(entry.key) &&
                Objects.equals(value, entry.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
}
