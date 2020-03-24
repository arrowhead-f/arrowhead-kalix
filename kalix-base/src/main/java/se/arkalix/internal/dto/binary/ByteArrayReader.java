package se.arkalix.internal.dto.binary;

import se.arkalix.dto.binary.BinaryReader;

public class ByteArrayReader implements BinaryReader {
    private final byte[] array;

    private int offset;
    private int limit;

    public ByteArrayReader(final byte[] array) {
        this.array = array;
        offset = 0;
        limit = array.length;
    }

    public void readLimit(final int limit) {
        this.limit = limit;
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
        return limit - offset;
    }

    @Override
    public void getBytes(final int offset, final byte[] target) {
        if (offset + target.length > limit) {
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
        if (offset + length > limit) {
            throw new IndexOutOfBoundsException("targetOffset + length > readableBytes()");
        }
        System.arraycopy(array, offset, target, targetOffset, length);
    }

    @Override
    public void skipBytes(final int n) {
        offset += n;
    }
}
