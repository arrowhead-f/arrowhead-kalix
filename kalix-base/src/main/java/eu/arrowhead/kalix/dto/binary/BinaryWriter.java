package eu.arrowhead.kalix.dto.binary;

import java.nio.ByteBuffer;

public interface BinaryWriter {
    int writeOffset();

    void writeOffset(int offset);

    void write(byte b);

    void write(byte[] bytes);

    static BinaryWriter from(final ByteBuffer buffer) {
        return new BinaryWriter() {
            @Override
            public int writeOffset() {
                return buffer.position();
            }

            @Override
            public void writeOffset(final int offset) {
                buffer.position(offset);
            }

            @Override
            public void write(final byte b) {
                buffer.put(b);
            }

            @Override
            public void write(final byte[] bytes) {
                buffer.put(bytes);
            }
        };
    }
}
