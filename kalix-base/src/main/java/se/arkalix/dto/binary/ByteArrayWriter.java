package se.arkalix.dto.binary;

import java.util.Objects;

public class ByteArrayWriter implements BinaryWriter {
    private final byte[] byteArray;

    private int offset = 0;

    public ByteArrayWriter(final byte[] byteArray) {
        this.byteArray = Objects.requireNonNull(byteArray, "Expected byteArray");
    }

    @Override
    public int writeOffset() {
        return offset;
    }

    @Override
    public void writeOffset(final int offset) {
        if (offset < 0 || offset > byteArray.length) {
            throw new IndexOutOfBoundsException("offset < 0 || offset > byteArray.length");
        }
        this.offset = offset;
    }

    @Override
    public int writableBytes() {
        return byteArray.length - offset;
    }

    @Override
    public void write(final byte b) {
        if (writableBytes() < 1) {
            throw new IndexOutOfBoundsException("No writable bytes remaining");
        }
        byteArray[offset++] = b;
    }

    @Override
    public void write(final byte[] bytes) {
        if (writableBytes() < bytes.length) {
            throw new IndexOutOfBoundsException("Not enough writable bytes remaining to write all given bytes");
        }
        System.arraycopy(bytes, 0, byteArray, offset, bytes.length);
        offset += bytes.length;
    }

    public byte[] asByteArray() {
        return byteArray;
    }
}
