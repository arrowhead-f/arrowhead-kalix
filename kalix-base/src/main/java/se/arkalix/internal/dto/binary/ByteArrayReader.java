package se.arkalix.internal.dto.binary;

import se.arkalix.dto.binary.BinaryReader;

public class ByteArrayReader implements BinaryReader {
    private final byte[] array;

    private int offset;

    public ByteArrayReader(final byte[] array) {
        this.array = array;
        offset = 0;
    }

    public ByteArrayReader(final byte[] array, int offset) {
        this.array = array;
        this.offset = offset;
    }

    @Override
    public int readOffset() {
        return offset;
    }

    @Override
    public void readOffset(final int offset) {
        this.offset = offset;
    }

    @Override
    public int readableBytes() {
        return array.length - offset;
    }

    @Override
    public void getBytes(final int offset, final byte[] target) {
        if (offset + target.length > array.length) {
            throw new IndexOutOfBoundsException("offset + target.length > readableBytes()");
        }
        System.arraycopy(array, offset, target, 0, target.length);
    }

    @Override
    public byte peekByte() {
        return array[offset];
    }

    @Override
    public byte readByte() {
        return array[offset++];
    }

    @Override
    public void readBytes(final byte[] target, final int targetOffset, final int length) {
        if (length > target.length) {
            throw new IndexOutOfBoundsException("target.length < length");
        }
        if (offset + length > array.length) {
            throw new IndexOutOfBoundsException("targetOffset + length > readableBytes()");
        }
        System.arraycopy(array, offset, target, targetOffset, length);
    }

    @Override
    public void skipBytes(final int n) {
        offset += n;
    }
}
