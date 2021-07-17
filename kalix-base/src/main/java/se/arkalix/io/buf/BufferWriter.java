package se.arkalix.io.buf;

import se.arkalix.util._internal.BinaryMath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;

/**
 * A collection of memory that can be written to.
 * <p>
 * Keeps track of a {@link #writeOffset() write offset}, which is increased
 * automatically whenever a write operation is successfully performed on this
 * buffer. It also keeps tack of a {@link #writeEnd() write end}, which may
 * never be passed by the write offset.
 * <p>
 * The below diagram illustrates how this works in practice. The buffer consists
 * of a sequence of bytes, denoted by squares in the diagram. Each byte has an
 * <i>offset</i> relative to the beginning of the memory, as well as a current
 * <i>value</i>. The write offset and end both point to offsets within the
 * buffer. Whenever a byte is written to the buffer, the byte at the read offset
 * is updated and the read offset itself is moved one step closer to the end
 * offset. If the read offset reaches the end offset, all subsequent write
 * operations will fail.
 * <pre>
 *   Offset: 0   1   2   3   4   5   6   7
 *           +---+---+---+---+---+---+---+---+
 *    Value: | 9 | 3 | 0 | 0 | 0 | 0 | 0 | 0 |
 *           +---+---+---+---+---+---+---+---+
 *                   A                       A
 *                   |                       |
 *              Write Offset             Write End
 * </pre>
 * To support writing to this buffer without affecting its internal write
 * offset, this interface also provides numerous methods prefixed with {@code
 * set}, all of which requires that a buffer offset is explicitly provided.
 *
 * @see Buffer
 * @see BufferReader
 */
@SuppressWarnings("unused")
public interface BufferWriter extends AutoCloseable {
    @Override
    void close();

    /**
     * Determines if this buffer has been {@link #close() closed}.
     *
     * @return {@code true} only if this buffer is {@link #close() closed}.
     */
    boolean isClosed();

    default void setAt(final int offset, final byte[] source) {
        setAt(offset, source, 0, source.length);
    }

    default void setAt(final int offset, final byte[] source, final int sourceOffset, final int length) {
        try (final var sourceBuffer = Buffer.wrap(source)) {
            setAt(offset, sourceBuffer, sourceOffset, length);
        }
    }

    default void setAt(final int offset, final BufferReader source) {
        setAt(offset, source, source.readableBytes());
    }

    default void setAt(int offset, BufferReader source, int length) {
        setAt(offset, source, source.readOffset(), length);
        source.readOffset(source.readOffset() + length);
    }

    void setAt(int offset, BufferReader source, int sourceOffset, int length);

    void setAt(int offset, ByteBuffer source);

    default int setAt(final int offset, final ReadableByteChannel source) {
        return setAt(offset, source, writableBytes());
    }

    int setAt(int offset, ReadableByteChannel source, int maxLength);

    default void setF32BeAt(final int offset, final float value) {
        setS32BeAt(offset, Float.floatToIntBits(value));
    }

    default void setF32LeAt(final int offset, final float value) {
        setS32LeAt(offset, Float.floatToIntBits(value));
    }

    default void setF32NeAt(final int offset, final float value) {
        setS32NeAt(offset, Float.floatToIntBits(value));
    }

    default void setF64BeAt(final int offset, final double value) {
        setS64BeAt(offset, Double.doubleToLongBits(value));
    }

    default void setF64LeAt(final int offset, final double value) {
        setS64LeAt(offset, Double.doubleToLongBits(value));
    }

    default void setF64NeAt(final int offset, final double value) {
        setS64NeAt(offset, Double.doubleToLongBits(value));
    }

    default void setS8At(final int offset, final byte value) {
        setAt(offset, new byte[]{value});
    }

    default void setS16BeAt(final int offset, final short value) {
        final var buffer = new byte[2];
        BinaryMath.setS16BeAt(buffer, 0, value);
        setAt(offset, buffer);
    }

    default void setS16LeAt(int offset, short value) {
        final var buffer = new byte[2];
        BinaryMath.setS16LeAt(buffer, 0, value);
        setAt(offset, buffer);
    }

    default void setS16NeAt(final int offset, final short value) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            setS16BeAt(offset, value);
        }
        else {
            setS16LeAt(offset, value);
        }
    }

    default void setS24BeAt(final int offset, final int value) {
        final var buffer = new byte[3];
        BinaryMath.setS24BeAt(buffer, 0, value);
        setAt(offset, buffer);
    }

    default void setS24LeAt(final int offset, final int value) {
        final var buffer = new byte[3];
        BinaryMath.setS24LeAt(buffer, 0, value);
        setAt(offset, buffer);
    }

    default void setS24NeAt(final int offset, final int value) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            setS24BeAt(offset, value);
        }
        else {
            setS24LeAt(offset, value);
        }
    }

    default void setS32BeAt(final int offset, final int value) {
        final var buffer = new byte[4];
        BinaryMath.setS32BeAt(buffer, 0, value);
        setAt(offset, buffer);
    }

    default void setS32LeAt(final int offset, final int value) {
        final var buffer = new byte[4];
        BinaryMath.setS32LeAt(buffer, 0, value);
        setAt(offset, buffer);
    }

    default void setS32NeAt(final int offset, final int value) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            setS32BeAt(offset, value);
        }
        else {
            setS32LeAt(offset, value);
        }
    }

    default void setS48BeAt(final int offset, final long value) {
        final var buffer = new byte[6];
        BinaryMath.setS48BeAt(buffer, 0, value);
        setAt(offset, buffer);
    }

    default void setS48LeAt(final int offset, final long value) {
        final var buffer = new byte[6];
        BinaryMath.setS48LeAt(buffer, 0, value);
        setAt(offset, buffer);
    }

    default void setS48NeAt(final int offset, final long value) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            setS48BeAt(offset, value);
        }
        else {
            setS48LeAt(offset, value);
        }
    }

    default void setS64BeAt(final int offset, final long value) {
        final var buffer = new byte[8];
        BinaryMath.setS64BeAt(buffer, 0, value);
        setAt(offset, buffer);
    }

    default void setS64LeAt(final int offset, final long value) {
        final var buffer = new byte[8];
        BinaryMath.setS64LeAt(buffer, 0, value);
        setAt(offset, buffer);
    }

    default void setS64NeAt(final int offset, final long value) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            setS64BeAt(offset, value);
        }
        else {
            setS64LeAt(offset, value);
        }
    }

    default void setU8At(final int offset, final int value) {
        if (value < 0 || value > 255) {
            throw new BufferValueOutOfBounds(0, 255, value);
        }
        setS8At(offset, (byte) value);
    }

    default void setU16BeAt(final int offset, final int value) {
        if (value < 0 || value > 65535) {
            throw new BufferValueOutOfBounds(0, 65535, value);
        }
        setS16BeAt(offset, (short) value);
    }

    default void setU16LeAt(final int offset, final int value) {
        if (value < 0 || value > 65535) {
            throw new BufferValueOutOfBounds(0, 65535, value);
        }
        setS16LeAt(offset, (short) value);
    }

    default void setU16NeAt(final int offset, final int value) {
        if (value < 0 || value > 65535) {
            throw new BufferValueOutOfBounds(0, 65535, value);
        }
        setS16NeAt(offset, (short) value);
    }

    default void setU24BeAt(final int offset, final int value) {
        if (value < 0 || value > 16777215) {
            throw new BufferValueOutOfBounds(0, 16777215, value);
        }
        setS24BeAt(offset, value);
    }

    default void setU24LeAt(final int offset, final int value) {
        if (value < 0 || value > 16777215) {
            throw new BufferValueOutOfBounds(0, 16777215, value);
        }
        setS24LeAt(offset, value);
    }

    default void setU24NeAt(final int offset, final int value) {
        if (value < 0 || value > 16777215) {
            throw new BufferValueOutOfBounds(0, 16777215, value);
        }
        setS24NeAt(offset, value);
    }

    default void setU32BeAt(final int offset, final long value) {
        if (value < 0 || value > 4294967295L) {
            throw new BufferValueOutOfBounds(0, 4294967295L, value);
        }
        setS32BeAt(offset, (int) value);
    }

    default void setU32LeAt(final int offset, final long value) {
        if (value < 0 || value > 4294967295L) {
            throw new BufferValueOutOfBounds(0, 4294967295L, value);
        }
        setS32LeAt(offset, (int) value);
    }

    default void setU32NeAt(final int offset, final long value) {
        if (value < 0 || value > 4294967295L) {
            throw new BufferValueOutOfBounds(0, 4294967295L, value);
        }
        setS32NeAt(offset, (int) value);
    }

    default void setU48BeAt(final int offset, final long value) {
        if (value < 0 || value > 281474976710655L) {
            throw new BufferValueOutOfBounds(0, 281474976710655L, value);
        }
        setS48BeAt(offset, value);
    }

    default void setU48LeAt(final int offset, final long value) {
        if (value < 0 || value > 281474976710655L) {
            throw new BufferValueOutOfBounds(0, 281474976710655L, value);
        }
        setS48LeAt(offset, value);
    }

    default void setU48NeAt(final int offset, final long value) {
        if (value < 0 || value > 281474976710655L) {
            throw new BufferValueOutOfBounds(0, 281474976710655L, value);
        }
        setS48NeAt(offset, value);
    }

    default void setU64BeAt(final int offset, final long value) {
        setS64BeAt(offset, value);
    }

    default void setU64LeAt(final int offset, final long value) {
        setS64LeAt(offset, value);
    }

    default void setU64NeAt(final int offset, final long value) {
        setS64NeAt(offset, value);
    }

    /**
     * Gets the number of bytes that can currently be written to this buffer,
     * assuming any required memory allocations will succeed.
     *
     * @return Current number of writable bytes.
     * @throws BufferIsClosed If this buffer is closed. Not guaranteed to be
     *                        thrown by all implementations.
     */
    default int writableBytes() {
        return writeEnd() - writeOffset();
    }

    /**
     * Gets copy of internal write offset, which determines the position of the
     * next byte written to this buffer.
     *
     * @return Copy of internal write offset.
     * @throws BufferIsClosed If this buffer is closed. Not guaranteed to be
     *                        thrown by all implementations.
     * @see #writeOffset(int)
     */
    int writeOffset();

    /**
     * Updates the internal write offset by setting it to the given value.
     *
     * @param writeOffset Desired new internal write offset.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code writeOffset < 0 ||
     *                                   writeOffset > writeEnd()}.
     * @see #writeOffset()
     */
    void writeOffset(int writeOffset);

    /**
     * Gets position of the first byte outside the range of this buffer that
     * could become writable.
     * <p>
     * In other words, this method returns the offset beyond which this buffer
     * can never grow. The offset can also be interpreted as the maximum size,
     * in bytes, of this buffer.
     *
     * @return Maximum buffer size.
     * @throws BufferIsClosed If this buffer is closed. Not guaranteed to be
     *                        thrown by all implementations.
     */
    int writeEnd();

    default void write(final byte[] source) {
        write(source, 0, source.length);
    }

    default void write(final byte[] source, final int sourceOffset, final int length) {
        try (final var sourceBuffer = Buffer.wrap(source)) {
            write(sourceBuffer, sourceOffset, length);
        }
    }

    default void write(final BufferReader source) {
        write(source, source.readableBytes());
    }

    default void write(BufferReader source, int length) {
        write(source, source.readOffset(), length);
        source.readOffset(source.readOffset() + length);
    }

    void write(BufferReader source, int sourceOffset, int length);

    void write(ByteBuffer source);

    default int write(final ReadableByteChannel source) {
        return write(source, writableBytes());
    }

    int write(ReadableByteChannel source, int maxLength);

    default void writeF32Be(final float value) {
        writeS32Be(Float.floatToIntBits(value));
    }

    default void writeF32Le(final float value) {
        writeS32Le(Float.floatToIntBits(value));
    }

    default void writeF32Ne(final float value) {
        writeS32Ne(Float.floatToIntBits(value));
    }

    default void writeF64Be(final double value) {
        writeS64Be(Double.doubleToLongBits(value));
    }

    default void writeF64Le(final double value) {
        writeS64Le(Double.doubleToLongBits(value));
    }

    default void writeF64Ne(final double value) {
        writeS64Ne(Double.doubleToLongBits(value));
    }

    default void writeS8(final byte value) {
        write(new byte[]{value});
    }

    default void writeS16Be(final short value) {
        final var buffer = new byte[2];
        BinaryMath.setS16BeAt(buffer, 0, value);
        write(buffer);
    }

    default void writeS16Le(final short value) {
        final var buffer = new byte[2];
        BinaryMath.setS16LeAt(buffer, 0, value);
        write(buffer);
    }

    default void writeS16Ne(final short value) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            writeS16Be(value);
        }
        else {
            writeS16Le(value);
        }
    }

    default void writeS24Be(final int value) {
        if (value < -8388608 || value > 8388607) {
            throw new BufferValueOutOfBounds(-8388608, 8388607, value);
        }
        final var buffer = new byte[3];
        BinaryMath.setS24BeAt(buffer, 0, value);
        write(buffer);
    }

    default void writeS24Le(final int value) {
        if (value < -8388608 || value > 8388607) {
            throw new BufferValueOutOfBounds(-8388608, 8388607, value);
        }
        final var buffer = new byte[3];
        BinaryMath.setS24LeAt(buffer, 0, value);
        write(buffer);
    }

    default void writeS24Ne(final int value) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            writeS24Be(value);
        }
        else {
            writeS24Le(value);
        }
    }

    default void writeS32Be(final int value) {
        final var buffer = new byte[4];
        BinaryMath.setS32BeAt(buffer, 0, value);
        write(buffer);
    }

    default void writeS32Le(final int value) {
        final var buffer = new byte[4];
        BinaryMath.setS32LeAt(buffer, 0, value);
        write(buffer);
    }

    default void writeS32Ne(final int value) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            writeS32Be(value);
        }
        else {
            writeS32Le(value);
        }
    }

    default void writeS48Be(final long value) {
        if (value < -140737488355328L || value > 140737488355327L) {
            throw new BufferValueOutOfBounds(-140737488355328L, 140737488355327L, value);
        }
        final var buffer = new byte[6];
        BinaryMath.setS48BeAt(buffer, 0, value);
        write(buffer);
    }

    default void writeS48Le(final long value) {
        if (value < -140737488355328L || value > 140737488355327L) {
            throw new BufferValueOutOfBounds(-140737488355328L, 140737488355327L, value);
        }
        final var buffer = new byte[6];
        BinaryMath.setS48LeAt(buffer, 0, value);
        write(buffer);
    }

    default void writeS48Ne(final long value) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            writeS48Be(value);
        }
        else {
            writeS48Le(value);
        }
    }

    default void writeS64Be(final long value) {
        final var buffer = new byte[8];
        BinaryMath.setS64BeAt(buffer, 0, value);
        write(buffer);
    }

    default void writeS64Le(final long value) {
        final var buffer = new byte[8];
        BinaryMath.setS64LeAt(buffer, 0, value);
        write(buffer);
    }

    default void writeS64Ne(final long value) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            writeS64Be(value);
        }
        else {
            writeS64Le(value);
        }
    }

    default void writeU8(final int value) {
        if (value < 0 || value > 255) {
            throw new BufferValueOutOfBounds(0, 255, value);
        }
        writeS8((byte) value);
    }

    default void writeU16Be(final int value) {
        if (value < 0 || value > 65535) {
            throw new BufferValueOutOfBounds(0, 65535, value);
        }
        writeS16Be((short) value);
    }

    default void writeU16Le(final int value) {
        if (value < 0 || value > 65535) {
            throw new BufferValueOutOfBounds(0, 65535, value);
        }
        writeS16Le((short) value);
    }

    default void writeU16Ne(final int value) {
        if (value < 0 || value > 65535) {
            throw new BufferValueOutOfBounds(0, 65535, value);
        }
        writeS16Ne((short) value);
    }

    default void writeU24Be(final int value) {
        if (value < 0 || value > 65535) {
            throw new BufferValueOutOfBounds(0, 65535, value);
        }
        writeS24Be(value);
    }

    default void writeU24Le(final int value) {
        if (value < 0 || value > 16777215) {
            throw new BufferValueOutOfBounds(0, 16777215, value);
        }
        writeS24Le(value);
    }

    default void writeU24Ne(final int value) {
        if (value < 0 || value > 16777215) {
            throw new BufferValueOutOfBounds(0, 16777215, value);
        }
        writeS24Ne(value);
    }

    default void writeU32Be(final long value) {
        if (value < 0 || value > 4294967295L) {
            throw new BufferValueOutOfBounds(0, 4294967295L, value);
        }
        writeS32Be((int) value);
    }

    default void writeU32Le(final long value) {
        if (value < 0 || value > 4294967295L) {
            throw new BufferValueOutOfBounds(0, 4294967295L, value);
        }
        writeS32Le((int) value);
    }

    default void writeU32Ne(final long value) {
        if (value < 0 || value > 4294967295L) {
            throw new BufferValueOutOfBounds(0, 4294967295L, value);
        }
        writeS32Ne((int) value);
    }

    default void writeU48Be(final long value) {
        if (value < 0 || value > 281474976710655L) {
            throw new BufferValueOutOfBounds(0, 281474976710655L, value);
        }
        writeS48Be(value);
    }

    default void writeU48Le(final long value) {
        if (value < 0 || value > 281474976710655L) {
            throw new BufferValueOutOfBounds(0, 281474976710655L, value);
        }
        writeS48Le(value);
    }

    default void writeU48Ne(final long value) {
        if (value < 0 || value > 281474976710655L) {
            throw new BufferValueOutOfBounds(0, 281474976710655L, value);
        }
        writeS48Ne(value);
    }

    default void writeU64Be(final long value) {
        writeS64Be(value);
    }

    default void writeU64Le(final long value) {
        writeS64Le(value);
    }

    default void writeU64Ne(final long value) {
        writeS64Ne(value);
    }
}
