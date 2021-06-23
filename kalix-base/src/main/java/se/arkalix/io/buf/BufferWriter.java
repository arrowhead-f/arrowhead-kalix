package se.arkalix.io.buf;

import java.nio.ByteBuffer;

/**
 * A collection of memory that can be written to.
 * <p>
 * Keeps track of a {@link #writeOffset() write offset}, which is increased
 * automatically whenever a write operation is successfully performed on this
 * buffer. It also keeps tack of a {@link #writeEnd() write end}, which may
 * never be passed by the write offset.
 * <p>
 * The below diagram illustrates how this works in practice. The buffer
 * consists of a sequence of bytes, denoted by squares in the diagram. Each
 * byte has an <i>offset</i> relative to the beginning of the memory, as well
 * as a current <i>value</i>. The write offset and end both point to offsets
 * within the buffer. Whenever a byte is written to the buffer, the byte at the
 * read offset is updated and the read offset itself is moved one step closer
 * to the end offset. If the read offset reaches the end offset, all subsequent
 * write operations will fail.
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

    void setAt(int offset, byte[] source, int sourceOffset, int length);

    default void setAt(final int offset, final BufferReader source) {
        setAt(offset, source, source.readableBytes());
    }

    default void setAt(int offset, BufferReader source, int length) {
        setAt(offset, source, source.readOffset(), length);
        source.readOffset(source.readOffset() + length);
    }

    void setAt(int offset, BufferReader source, int sourceOffset, int length);

    void setAt(int offset, ByteBuffer source);

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

    void setS8At(int offset, byte value);

    void setS16BeAt(int offset, short value);

    void setS16NeAt(int offset, short value);

    void setS16LeAt(int offset, short value);

    void setS24BeAt(int offset, int value);

    void setS24NeAt(int offset, int value);

    void setS24LeAt(int offset, int value);

    void setS32BeAt(int offset, int value);

    void setS32NeAt(int offset, int value);

    void setS32LeAt(int offset, int value);

    void setS48BeAt(int offset, long value);

    void setS48NeAt(int offset, long value);

    void setS48LeAt(int offset, long value);

    void setS64BeAt(int offset, long value);

    void setS64NeAt(int offset, long value);

    void setS64LeAt(int offset, long value);

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

    void write(byte[] source, int sourceOffset, int length);

    default void write(final BufferReader source) {
        write(source, source.readableBytes());
    }

    default void write(BufferReader source, int length) {
        write(source, source.readOffset(), length);
        source.readOffset(source.readOffset() + length);
    }

    void write(BufferReader source, int sourceOffset, int length);

    void write(ByteBuffer source);

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

    void writeS8(byte value);

    void writeS16Be(short value);

    void writeS16Le(short value);

    void writeS16Ne(short value);

    void writeS24Be(int value);

    void writeS24Le(int value);

    void writeS24Ne(int value);

    void writeS32Be(int value);

    void writeS32Le(int value);

    void writeS32Ne(int value);

    void writeS48Be(long value);

    void writeS48Le(long value);

    void writeS48Ne(long value);

    void writeS64Be(long value);

    void writeS64Le(long value);

    void writeS64Ne(long value);

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
