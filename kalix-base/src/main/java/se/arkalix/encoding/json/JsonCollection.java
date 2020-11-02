package se.arkalix.encoding.json;

import java.util.Optional;

/**
 * A JSON array of object.
 */
public interface JsonCollection<K> extends JsonValue {
    /**
     * @return {@code true} only if this array or object has no elements or
     * pairs, respectively.
     */
    boolean isEmpty();

    /**
     * @return The number of elements or pairs in this collection.
     */
    int size();

    /**
     * Gets collection value associated with given {@code key}.
     *
     * @param key Key associated with requested value.
     * @return Requested value, if it is available.
     */
    Optional<JsonValue> get(K key);
}
