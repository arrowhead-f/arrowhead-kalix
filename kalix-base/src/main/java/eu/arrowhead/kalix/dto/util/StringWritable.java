package eu.arrowhead.kalix.dto.util;

import eu.arrowhead.kalix.dto.DataWritable;

import java.util.Objects;

public class StringWritable implements DataWritable {
    private final String string;

    public StringWritable(final String string) {
        this.string = Objects.requireNonNull(string, "Expected string");
    }

    public String string() {
        return string;
    }
}
