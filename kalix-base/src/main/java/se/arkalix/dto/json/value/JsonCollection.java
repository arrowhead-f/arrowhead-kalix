package se.arkalix.dto.json.value;

/**
 * A JSON array of object.
 */
public interface JsonCollection extends JsonValue {
    /**
     * @return {@code true} only if this array or object has no elements or
     * pairs, respectively.
     */
    boolean isEmpty();

    /**
     * @return The number of elements or pairs in this collection.
     */
    int size();
}
