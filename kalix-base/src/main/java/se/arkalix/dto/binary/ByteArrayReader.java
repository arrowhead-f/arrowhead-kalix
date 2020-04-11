package se.arkalix.dto.binary;

import java.util.Objects;

/**
 * A {@link BinaryReader} that reads from a plain byte array.
 * <p>
 * This reader is naive in the sense that it does not check for range errors
 * before executing on any contained byte array. However, as range errors are
 * checked by Java on byte array operations, the error messages should still be
 * quite informative for most categories of errors.
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
        this.offset = offset;
    }

    @Override
    public int readableBytes() {
        return byteArray.length - offset;
    }

    @Override
    public byte getByte(final int offset) {
        return byteArray[offset];
    }

    @Override
    public void getBytes(final int offset, final byte[] target) {
        System.arraycopy(byteArray, offset, target, 0, target.length);
    }

    @Override
    public void getBytes(final int offset, final byte[] target, final int targetOffset, final int length) {
        System.arraycopy(byteArray, offset, target, targetOffset, length);
    }

    @Override
    public byte peekByte() {
        return byteArray[offset];
    }

    @Override
    public byte readByte() {
        return byteArray[offset++];
    }

    @Override
    public void readBytes(final byte[] target, final int targetOffset, final int length) {
        System.arraycopy(byteArray, offset, target, targetOffset, length);
        offset += length;
    }

    @Override
    public void skipBytes(final int n) {
        offset += n;
    }

    /**
     * @return Byte array contained in this reader.
     */
    public byte[] asByteArray() {
        return byteArray;
    }
}
