package eu.arrowhead.kalix.dto.data;

import eu.arrowhead.kalix.dto.DataReadable;
import eu.arrowhead.kalix.dto.DataWritable;

import java.util.Objects;

public final class DataByteArray implements DataReadable, DataWritable {
    private final byte[] byteArray;

    public DataByteArray(final byte[] byteArray) {
        this.byteArray = Objects.requireNonNull(byteArray, "Expected byteArray");
    }

    public byte[] byteArray() {
        return byteArray;
    }
}
