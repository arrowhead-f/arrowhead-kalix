package se.arkalix.dto.binary;

import java.util.function.Predicate;

public interface BinaryReader {
    int readOffset();

    void readOffset(int offset);

    int readableBytes();

    byte getByte(int offset);

    void getBytes(int offset, byte[] target);

    void getBytes(final int offset, final byte[] target, final int targetOffset, final int length);

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

    default void skipWhile(final Predicate<Byte> predicate) {
        while (readableBytes() > 0 && predicate.test(peekByte())) {
            skipByte();
        }
    }
}
