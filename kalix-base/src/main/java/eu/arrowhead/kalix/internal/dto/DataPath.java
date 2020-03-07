package eu.arrowhead.kalix.internal.dto;

import eu.arrowhead.kalix.dto.DataWritable;

import java.nio.file.Path;
import java.util.Objects;

public final class DataPath implements DataWritable {
    private final Path path;

    public DataPath(final Path path) {
        this.path = Objects.requireNonNull(path, "Expected path");
    }

    public Path path() {
        return path;
    }
}
