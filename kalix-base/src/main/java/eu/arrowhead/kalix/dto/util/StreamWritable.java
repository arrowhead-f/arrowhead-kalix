package eu.arrowhead.kalix.dto.util;

import eu.arrowhead.kalix.dto.DataWritable;

import java.io.InputStream;
import java.util.Objects;

public class StreamWritable implements DataWritable {
    private final InputStream stream;

    public StreamWritable(final InputStream stream) {
        this.stream = Objects.requireNonNull(stream, "Expected stream");
    }

    public InputStream stream() {
        return stream;
    }
}
