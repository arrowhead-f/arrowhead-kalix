package eu.arrowhead.kalix.dto.data;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.dto.DataWritable;

import java.nio.file.Path;
import java.util.Objects;

public class DataPath implements DataReadable, DataWritable {
    private final Path path;

    public DataPath(final Path path) {
        this.path = Objects.requireNonNull(path, "Expected path");
    }

    public Path path() {
        return path;
    }
}
