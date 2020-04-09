package se.arkalix.dto.json.value;

import java.util.Objects;

@SuppressWarnings("unused")
public class JsonPair {
    private final String name;
    private final JsonValue value;

    public JsonPair(final String name, final JsonValue value) {
        this.name = Objects.requireNonNull(name, "Expected name");
        this.value = Objects.requireNonNullElse(value, JsonNull.instance());
    }

    public String name() {
        return name;
    }

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
