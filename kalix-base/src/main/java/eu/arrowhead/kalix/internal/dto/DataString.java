package eu.arrowhead.kalix.internal.dto;

import eu.arrowhead.kalix.dto.DataWritable;

import java.util.Objects;

public final class DataString implements DataWritable {
    private final String string;

    public DataString(final String string) {
        this.string = Objects.requireNonNull(string, "Expected string");
    }

    public String string() {
        return string;
    }
}
