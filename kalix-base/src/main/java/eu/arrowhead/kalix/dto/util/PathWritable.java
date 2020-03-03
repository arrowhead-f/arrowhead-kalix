package eu.arrowhead.kalix.dto.util;

import eu.arrowhead.kalix.dto.DataWritable;

import java.nio.file.Path;
import java.util.Objects;

public class PathWritable implements DataWritable {
    private final Path path;

    public PathWritable(final Path path) {
        this.path = Objects.requireNonNull(path, "Expected path");
    }

    public Path path() {
        return path;
    }
}
