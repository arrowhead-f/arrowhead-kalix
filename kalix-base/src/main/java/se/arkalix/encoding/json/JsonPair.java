package se.arkalix.encoding.json;

import java.util.Objects;

/**
 * A pair, potentially a member of a {@link JsonObject JSON object}.
 */
@SuppressWarnings("unused")
public class JsonPair {
    private final String name;
    private final JsonValue value;

    /**
     * Creates new pair from given name and value.
     *
     * @param name  Pair name.
     * @param value Pair value.
     */
    public JsonPair(final String name, final JsonValue value) {
        this.name = Objects.requireNonNull(name, "name");
        this.value = Objects.requireNonNullElse(value, JsonNull.instance);
    }

    /**
     * @return Pair name.
     */
    public String name() {
        return name;
    }

    /**
     * @return Pair value.
     */
    public JsonValue value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final JsonPair jsonPair = (JsonPair) o;
        return name.equals(jsonPair.name) &&
            value.equals(jsonPair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return name + ": " + value;
    }
}
