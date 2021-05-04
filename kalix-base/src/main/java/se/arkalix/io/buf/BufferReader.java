package se.arkalix.io.buf;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public interface BufferReader extends AutoCloseable {
    ByteBuffer asByteBuffer();

    @Override
    void close();

    boolean isClosed();

    default void getAt(final int offset, final byte[] destination) {
        getAt(offset, destination, 0, destination.length);
    }

    void getAt(int offset, byte[] destination, int destinationOffset, int length);

    default void getAt(final int offset, final BufferWriter destination) {
        getAt(offset, destination, destination.writableBytes());
    }

    default void getAt(int offset, BufferWriter destination, int length) {
        getAt(offset, destination, destination.writeOffset(), length);
        destination.writeOffset(destination.writeOffset() + length);
    }

    void getAt(int offset, BufferWriter destination, int destinationOffset, int length);

    void getAt(int offset, ByteBuffer destination);

    default float getF32At(final int offset) {
        return Float.intBitsToFloat(getS32NeAt(offset));
    }

    default double getF64At(final int offset) {
        return Double.longBitsToDouble(getS64NeAt(offset));
    }

    byte getS8At(int offset);

    short getS16BeAt(int offset);

    short getS16LeAt(int offset);

    short getS16NeAt(int offset);

    default int getS24BeAt(int offset) {
        var value = getU24BeAt(offset);
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    default int getS24LeAt(int offset) {
        var value = getU24LeAt(offset);
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    default int getS24NeAt(int offset) {
        var value = getU24NeAt(offset);
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    int getS32BeAt(int offset);

    int getS32LeAt(int offset);

    int getS32NeAt(int offset);

    default long getS48BeAt(int offset) {
        var value = getU48BeAt(offset);
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }

    default long getS48LeAt(int offset) {
        var value = getU48LeAt(offset);
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;

    }

    default long getS48NeAt(int offset) {
        var value = getU48NeAt(offset);
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }

    long getS64BeAt(int offset);

    long getS64LeAt(int offset);

    long getS64NeAt(int offset);

    default int getU8At(final int offset) {
        return Byte.toUnsignedInt(getS8At(offset));
    }

    default int getU16BeAt(final int offset) {
        return Short.toUnsignedInt(getS16BeAt(offset));
    }

    default int getU16NeAt(final int offset) {
        return Short.toUnsignedInt(getS16NeAt(offset));
    }

    default int getU16LeAt(final int offset) {
        return Short.toUnsignedInt(getS16LeAt(offset));
    }

    int getU24BeAt(final int offset);

    int getU24NeAt(final int offset);

    int getU24LeAt(final int offset);

    default long getU32BeAt(final int offset) {
        return Integer.toUnsignedLong(getS32BeAt(offset));
    }

    default long getU32NeAt(final int offset) {
        return Integer.toUnsignedLong(getS32NeAt(offset));
    }

    default long getU32LeAt(final int offset) {
        return Integer.toUnsignedLong(getS32LeAt(offset));
    }

    long getU48BeAt(final int offset);

    long getU48NeAt(final int offset);

    long getU48LeAt(final int offset);

    default long getU64BeAt(final int offset) {
        return getS64BeAt(offset);
    }

    default long getU64LeAt(final int offset) {
        return getS64LeAt(offset);
    }

    default long getU64NeAt(final int offset) {
        return getS64NeAt(offset);
    }

    int readableBytes();

    int readOffset();

    void readOffset(int readOffset);

    int readEnd();

    default void read(final byte[] destination) {
        read(destination, 0, destination.length);
    }

    void read(byte[] destination, int destinationOffset, int length);

    default void read(final BufferWriter destination) {
        read(destination, destination.writableBytes());
    }

    default void read(final BufferWriter destination, final int length) {
        read(destination, destination.writeOffset(), length);
        destination.writeOffset(destination.writeOffset() + length);
    }

    void read(BufferWriter destination, int destinationOffset, int length);

    void read(ByteBuffer destination);

    default float readF32() {
        return Float.intBitsToFloat(readS32Ne());
    }

    default double readF64() {
        return Double.longBitsToDouble(readS64Ne());
    }

    byte readS8();

    short readS16Be();

    short readS16Le();

    short readS16Ne();

    default int readS24Be() {
        var value = readU24Be();
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    default int readS24Le() {
        var value = readU24Le();
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    default int readS24Ne() {
        var value = readU24Ne();
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    int readS32Be();

    int readS32Le();

    int readS32Ne();

    default long readS48Be() {
        var value = readU48Be();
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }

    default long readS48Le() {
        var value = readU48Le();
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }

    default long readS48Ne() {
        var value = readU48Ne();
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }

    long readS64Be();

    long readS64Ne();

    long readS64Le();

    default int readU8() {
        return Byte.toUnsignedInt(readS8());
    }

    default int readU16Be() {
        return Short.toUnsignedInt(readS16Be());
    }

    default int readU16Le() {
        return Short.toUnsignedInt(readS16Le());
    }

    default int readU16Ne() {
        return Short.toUnsignedInt(readS16Ne());
    }

    int readU24Be();

    int readU24Le();

    int readU24Ne();

    default long readU32Be() {
        return Integer.toUnsignedLong(readS32Be());
    }

    default long readU32Le() {
        return Integer.toUnsignedLong(readS32Le());
    }

    default long readU32Ne() {
        return Integer.toUnsignedLong(readS32Ne());
    }

    long readU48Be();

    long readU48Le();

    long readU48Ne();

    default long readU64Be() {
        return readS64Be();
    }

    default long readU64Le() {
        return readS64Le();
    }

    default long readU64Ne() {
        return readS64Ne();
    }

    void skip(int bytesToSkip);
}
