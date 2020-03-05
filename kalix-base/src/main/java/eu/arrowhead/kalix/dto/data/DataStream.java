package eu.arrowhead.kalix.dto.data;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.dto.DataWritable;

import java.io.InputStream;

public final class DataStream implements DataReadable, DataWritable {
    private final InputStream stream;

    public DataStream(final InputStream stream) {
        this.stream = stream;
    }

    public InputStream stream() {
        return stream;
    }
}
