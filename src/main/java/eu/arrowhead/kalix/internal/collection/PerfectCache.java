package eu.arrowhead.kalix.internal.collection;

import eu.arrowhead.kalix.internal.util.BinaryMath;

/**
 * A map whose entries are guaranteed to be accessible in O(1).
 * <p>
 * To achieve this, a so-called <i>perfect hashtable</i> i used. Such a table
 * can only be reliably constructed from a well-known set of keys, and is,
 * therefore, almost only practically useful for caching data known at
 * compile-time. It is, for example, useful for caching strings that are
 * expected to be parsed frequently during runtime.
 *
 * @see #PerfectCache(int, int, Entry...)
 */
public class PerfectCache {
    private final Entry[] entries;
    private final int mask;
    private final int rotate;

    /**
     * Constructs new perfect hash table from given collection of
     * {@code entries}.
     * <p>
     * In the case of construction failing with an exception, try arbitrary
     * values between 0 and 31 for {@code rotate} until one is found that does
     * not result in an exception being thrown. If no such value can be found,
     * try increasing {@code capacity} to a power of two larger than the number
     * of entries you intend to keep in the cache. Another option might be to
     * try changing changing, adding or removing keys from {@code entries}. The
     * reason for this ceremony is to make sure that all entry keys yield
     * unique hash values if provided to the following algorithm:
     * <pre>
     *     Integer.rotateRight(key.hashCode(), rotate) & mask
     * </pre>
     * The {@code mask} is adjusted automatically to be one minus the smallest
     * power of two that is equal to or larger than the length of
     * {@code entries} or given {@code size}. By providing new {@code rotate}
     * values, you are making the algorithm take new regions of the generated
     * hash codes into account. By increasing {@code size}, you are increasing
     * the number of bits in the hashes.
     *
     * @param rotate   Arbitrary hashing adjustment between 0 and 31.
     * @param capacity Minimum capacity of created hash map.
     * @param entries  Entries from which to construct perfect cache.
     */
    public PerfectCache(final int rotate, final int capacity, final Entry... entries) {
        this.entries = new Entry[BinaryMath.findNextPositivePowerOfTwo(Math.max(entries.length, capacity))];
        this.mask = this.entries.length - 1;
        this.rotate = rotate;

        for (final var entry : entries) {
            final var index = (entry.key.hashCode() >>> rotate) & mask;
            if (this.entries[index] == null) {
                this.entries[index] = entry;
                continue;
            }
            // Adjust `rotate` or `capacity` to avoid this as necessary.
            throw new IllegalArgumentException(
                "Encoding cache hash collision; `" + this.entries[index].key
                    + "` and `" + entry.key
                    + "` have the same masked string hash codes");
        }
    }

    /**
     * @param key Key, potentially associated with a value in cache.
     * @return Associated value, or {@code null} if key is not present in cache.
     */
    public Object get(final Object key) {
        final var entry = entries[Integer.rotateRight(key.hashCode(), rotate) & mask];
        return entry != null && entry.key.equals(key) ? entry.value : null;
    }

    /**
     * Perfect cache entry.
     */
    public static final class Entry {
        private final Object key;
        private final Object value;

        /**
         * Creates new perfect cache entry.
         * <p>
         * Given key <i>should</i> override {@link Object#hashCode()}, or
         * entry lookup may not work as expected.
         *
         * @param key   Entry key.
         * @param value Entry value.
         */
        public Entry(final Object key, final Object value) {
            this.key = key;
            this.value = value;
        }
    }
}
