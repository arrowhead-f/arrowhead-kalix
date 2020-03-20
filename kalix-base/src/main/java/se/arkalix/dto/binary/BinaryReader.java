package se.arkalix.dto.binary;

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
}
