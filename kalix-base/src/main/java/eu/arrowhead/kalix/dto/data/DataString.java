package eu.arrowhead.kalix.dto.data;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.dto.DataWritable;

import java.util.Objects;

public final class DataString implements DataReadable, DataWritable {
    private final String string;

    public DataString(final String string) {
        this.string = Objects.requireNonNull(string, "Expected string");
    }

    public String string() {
        return string;
    }
}
