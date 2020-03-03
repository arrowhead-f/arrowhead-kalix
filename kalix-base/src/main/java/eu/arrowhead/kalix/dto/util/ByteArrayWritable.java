package eu.arrowhead.kalix.dto.util;

import eu.arrowhead.kalix.dto.DataWritable;

import java.util.Objects;

public class ByteArrayWritable implements DataWritable {
    private final byte[] byteArray;

    public ByteArrayWritable(final byte[] byteArray) {
        this.byteArray = Objects.requireNonNull(byteArray, "Expected byteArray");
    }

    public byte[] byteArray() {
        return byteArray;
    }
}
