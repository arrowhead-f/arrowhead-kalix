package se.arkalix.dto.json.value;

@SuppressWarnings("unused")
public class JsonPair {
    private final String name;
    private final JsonValue value;

    public JsonPair(final String name, final JsonValue value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public JsonValue value() {
        return value;
    }
}
