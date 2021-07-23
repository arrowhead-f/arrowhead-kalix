package se.arkalix.util.logging;

import java.util.Objects;

public record Attribute(String label, Object value) {
    public Attribute(final String label, final Object value) {
        this.label = Objects.requireNonNull(label, "label");
        this.value = value;
    }

    @Override
    public String toString() {
        return label + '=' + (value instanceof CharSequence
            ? ('\'' + value.toString() + '\'')
            : value);
    }
}
