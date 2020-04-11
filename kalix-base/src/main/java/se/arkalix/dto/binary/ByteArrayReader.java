package se.arkalix.dto.binary;

import java.util.Objects;

/**
 * A {@link BinaryReader} that reads from a plain byte array.
 */
public class ByteArrayReader implements BinaryReader {
    private final byte[] byteArray;

    private int offset = 0;

    /**
     * Creates new byte array reader.
     *
     * @param byteArray Byte array to read.
     */
    public ByteArrayReader(final byte[] byteArray) {
        this.byteArray = Objects.requireNonNull(byteArray, "Expected byteArray");
    }

    @Override
    public int readOffset() {
        return offset;
    }

    @Override
    public void readOffset(final int offset) {
        if (offset < 0 || offset > byteArray.length) {
            throw new IndexOutOfBoundsException("offset < 0 || offset > byteArray.length");
        }
        this.offset = offset;
    }

    @Override
    public int readableBytes() {
        return byteArray.length - offset;
    }

    @Override
    public byte getByte(final int offset) {
        if (offset < 0 || offset > byteArray.length) {
            throw new IndexOutOfBoundsException("offset < 0 || offset > byteArray.length");
        }
        return byteArray[offset];
    }

    @Override
    public void getBytes(final int offset, final byte[] target) {
        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset < 0");
        }
        if (offset > byteArray.length - target.length) {
            throw new IndexOutOfBoundsException("Not enough bytes available from offset to fill target");
        }
        System.arraycopy(byteArray, offset, target, 0, target.length);
    }

    @Override
    public void getBytes(final int offset, final byte[] target, final int targetOffset, final int length) {
        if (offset < 0 || targetOffset < 0) {
            throw new IndexOutOfBoundsException("offset < 0 || targetOffset < 0");
        }
        if (offset > byteArray.length - length) {
            throw new IndexOutOfBoundsException("Less than length bytes available from offset");
        }
        if (targetOffset > target.length) {
            throw new IndexOutOfBoundsException("targetOffset > target.length");
        }
        if (length < 0 || length > target.length) {
            throw new IndexOutOfBoundsException("length < 0 || length > target.length");
        }
        System.arraycopy(byteArray, offset, target, targetOffset, length);
    }

    @Override
    public byte peekByte() {
        return byteArray[offset];
    }

    @Override
    public byte readByte() {
        if (readableBytes() < 1) {
            throw new IndexOutOfBoundsException("No bytes to read");
        }
        return byteArray[offset++];
    }

    @Override
    public void readBytes(final byte[] target, final int targetOffset, final int length) {
        if (targetOffset < 0 || targetOffset > target.length) {
            throw new IndexOutOfBoundsException("targetOffset < 0 || targetOffset > target.length");
        }
        if (target.length < length) {
            throw new IndexOutOfBoundsException("target.length < length");
        }
        if (readableBytes() < length) {
            throw new IndexOutOfBoundsException("Less than `length` readable bytes remaining");
        }
        System.arraycopy(byteArray, offset, target, targetOffset, length);
        offset += length;
    }

    @Override
    public void skipBytes(final int n) {
        if (readableBytes() < n) {
            throw new IndexOutOfBoundsException("Less than `n` readable bytes remaining");
        }
        offset += n;
    }

    /**
     * @return Byte array contained in this reader.
     */
    public byte[] asByteArray() {
        return byteArray;
    }
}
