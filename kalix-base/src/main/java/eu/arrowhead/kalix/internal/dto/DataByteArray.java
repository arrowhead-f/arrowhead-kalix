package eu.arrowhead.kalix.internal.dto;

import eu.arrowhead.kalix.dto.DataWritable;

import java.util.Objects;

public final class DataByteArray implements DataWritable {
    private final byte[] byteArray;

    public DataByteArray(final byte[] byteArray) {
        this.byteArray = Objects.requireNonNull(byteArray, "Expected byteArray");
    }

    public byte[] byteArray() {
        return byteArray;
    }
}
