package se.arkalix.util.logging;

import java.util.Objects;

public class Attribute {
    private final String label;
    private final Object value;

    Attribute(final String label, final Object value) {
        this.label = Objects.requireNonNull(label, "label");
        this.value = value;
    }

    public String label() {
        return label;
    }

    public Object value() {
        return value;
    }

    @Override
    public String toString() {
        return label + '=' + (value instanceof CharSequence
            ? ('\'' + value.toString() + '\'')
            : value);
    }
}
