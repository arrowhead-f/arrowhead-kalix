package eu.arrowhead.kalix.dto.binary;

import java.nio.ByteBuffer;

public interface BinaryReader {
    int readOffset();

    void readOffset(int offset);

    int readableBytes();

    void getBytes(int offset, byte[] target);

    byte peekByte();

    byte readByte();

    default byte readByteOrZero() {
        return readableBytes() > 0
            ? readByte()
            : 0;
    }

    default void readBytes(byte[] target) {
        readBytes(target, 0, target.length);
    }

    void readBytes(byte[] target, int targetOffset, int length);

    default void skipByte() {
        skipBytes(1);
    }

    void skipBytes(int n);

    static BinaryReader from(final ByteBuffer buffer) {
        return new BinaryReader() {
            @Override
            public int readOffset() {
                return buffer.position();
            }

            @Override
            public void readOffset(final int offset) {
                buffer.position(offset);
            }

            @Override
            public int readableBytes() {
                return buffer.limit() - buffer.position();
            }

            @Override
            public void getBytes(final int offset, final byte[] target) {
                final var position = buffer.position();
                buffer.position(offset);
                buffer.get(target);
                buffer.position(position);
            }

            @Override
            public byte peekByte() {
                return buffer.get(buffer.position());
            }

            @Override
            public byte readByte() {
                return buffer.get();
            }

            @Override
            public void readBytes(final byte[] target, final int targetOffset, final int length) {
                buffer.get(target, targetOffset, length);
            }

            @Override
            public void skipBytes(final int n) {
                buffer.position(buffer.position() + n);
            }
        };
    }
}
