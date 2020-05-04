package se.arkalix.dto.binary;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * A {@link BinaryWriter} that writes to a plain byte array.
 * <p>
 * The underlying byte array will either be considered dynamic or static. If
 * dynamic, it will be reallocated as space runs out, while if static it will
 * throw {@link IndexOutOfBoundsException IndexOutOfBoundsExceptions} in the
 * same situation.
 * <p>
 * This writer is naive in the sense that it does not check for range errors
 * before executing on any contained byte array. However, as range errors are
 * checked by Java on byte array operations, the error messages should still be
 * quite informative for most categories of errors.
 */
@SuppressWarnings("unused")
public class ByteArrayWriter implements BinaryWriter {
    private final boolean isStatic;

    private byte[] byteArray;
    private int offset = 0;

    /**
     * Creates new empty <i>dynamically</i> sized byte array writer.
     */
    public ByteArrayWriter() {
        this(false, 0);
    }

    private ByteArrayWriter(final boolean isStatic, final int capacity) {
        this.isStatic = isStatic;
        byteArray = new byte[capacity];
    }

    /**
     * Creates new <i>statically</i> sized byte array writer.
     *
     * @param byteArray Byte array to write to.
     */
    public ByteArrayWriter(final byte[] byteArray) {
        isStatic = true;
        this.byteArray = Objects.requireNonNull(byteArray, "Expected byteArray");
    }

    /**
     * Creates new <i>dynamically</i> sized byte array writer with stated
     * initial capacity.
     *
     * @param capacity Number of bytes in initially allocated byte buffer.
     * @return New dynamic {@link ByteArrayWriter}.
     */
    public ByteArrayWriter dynamicWithCapacity(final int capacity) {
        return new ByteArrayWriter(false, capacity);
    }

    /**
     * Creates new <i>statically</i> sized byte array writer with stated final
     * capacity.
     *
     * @param capacity Number of bytes in allocated byte buffer.
     * @return New static {@link ByteArrayWriter}.
     */
    public ByteArrayWriter staticWithCapacity(final int capacity) {
        return new ByteArrayWriter(true, capacity);
    }

    @Override
    public int writeOffset() {
        return offset;
    }

    @Override
    public void writeOffset(final int offset) {
        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset < 0");
        }
        this.offset = offset;
    }

    @Override
    public int writableBytes() {
        return Math.max(0, byteArray.length - offset);
    }

    @Override
    public void write(final byte b) {
        if (offset >= byteArray.length) {
            if (isStatic) {
                throw new IndexOutOfBoundsException("No writable bytes remaining");
            }
            final var capacity = (byteArray.length + 4096) & ~4095;
            byteArray = Arrays.copyOf(byteArray, capacity);
        }
        byteArray[offset++] = b;
    }

    @Override
    public void write(final byte[] bytes) {
        final var newOffset = offset + bytes.length;
        if (newOffset > byteArray.length) {
            if (isStatic) {
                throw new IndexOutOfBoundsException("Not enough writable bytes remaining to write all given bytes");
            }
            final var capacity = (newOffset + 4096) & ~4095;
            byteArray = Arrays.copyOf(byteArray, capacity);
        }
        System.arraycopy(bytes, 0, byteArray, offset, bytes.length);
        offset += bytes.length;
    }

    /**
     * @return Byte array contained in this writer.
     */
    public byte[] asByteArray() {
        return byteArray;
    }

    public String toString() {
        return toString(StandardCharsets.UTF_8);
    }

    public String toString(final Charset charset) {
        return new String(byteArray, charset);
    }
}
