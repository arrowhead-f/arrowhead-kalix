package se.arkalix.io.buf;

import java.nio.ByteBuffer;

/**
 * A {@link Buffer} that can be read from.
 * <p>
 * Keeps track of an {@link #readOffset() internal read offset}, which is
 * increased automatically whenever a read operation is performed on this
 * buffer. It also keeps tack of a {@link #readEnd() internal read end}, which
 * may never be passed by the internal read offset.
 * <p>
 * The following diagram illustrates how this works in practice. The buffer
 * consists of a sequence of byte slots, denoted by squares in the diagram.
 * Each square has an <i>offset</i>, as well as a byte <i>value</i>. The
 * internal read offset and end both point to offsets within the buffer.
 * Whenever a byte is read from the buffer, the internal read offset is moved
 * closer to the internal end offset. If the buffer is written to through some
 * other class or interface, the internal read end may move further away from
 * the internal read offset.
 * <pre>
 *   Offset:   0   1   2   3   4   5   6   7   ...
 *           +---+---+---+---+---+---+---+---+-----+
 *    Value: | 0 | 0 | 5 | 1 | 9 | 0 | 1 | 0 | ... |
 *           +---+---+---+---+---+---+---+---+-----+
 *                 A               A
 *                 |               |
 *            Read Offset          |
 *                              Read End
 * </pre>
 * To support getting the contents of the buffer without its internal read
 * offset being updated, this interface also provides numerous {@code get*} and
 * {@code peek*} methods. The former category of methods read bytes from an
 * explicitly specified read offset, while the latter uses the internal read
 * offset without updating it.
 */
@SuppressWarnings("unused")
public interface BufferReader extends AutoCloseable {
    /**
     * Converts this buffer reader into a read-only NIO {@link ByteBuffer}.
     *
     * @return Read-only NIO {@link ByteBuffer}.
     * @throws BufferIsClosed If this buffer is closed.
     */
    ByteBuffer asReadOnlyByteBuffer();

    @Override
    void close();

    /**
     * Determines if this buffer reader has been {@link #close() closed}.
     *
     * @return {@code true} only if this buffer reader is {@link #close()
     * closed}.
     */
    boolean isClosed();

    /**
     * Copies {@code destination.length} bytes from this buffer, beginning at
     * {@code offset}, into {@code destination}.
     *
     * @param offset      Offset from beginning of this buffer.
     * @param destination Byte array to copy bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, destination.length]}
     *                                   is outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    default void getAt(final int offset, final byte[] destination) {
        getAt(offset, destination, 0, destination.length);
    }

    /**
     * Copies {@code length} bytes from this buffer, beginning at {@code
     * offset}, into {@code destination}, beginning at {@code
     * destinationOffset}.
     *
     * @param offset            Offset from beginning of this buffer.
     * @param destination       Byte array to copy bytes to.
     * @param destinationOffset Offset from beginning of {@code destination}.
     * @param length            Number of bytes to copy.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, length]} is outside
     *                                   the readable range of this buffer, or
     *                                   if {@code [destinationOffset, length]}
     *                                   is outside the range of {@code
     *                                   destination}.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    void getAt(int offset, byte[] destination, int destinationOffset, int length);

    /**
     * Gets {@code destination.writableBytes()} bytes from this buffer,
     * beginning at {@code offset}, and writes them to {@code destination}.
     * <p>
     * The internal write offset of {@code destination} is increased if this
     * operation is successful.
     *
     * @param offset      Offset from beginning of this buffer.
     * @param destination Buffer to write bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, destination.writableBytes()]}
     *                                   is outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    default void getAt(final int offset, final BufferWriter destination) {
        getAt(offset, destination, destination.writableBytes());
    }

    /**
     * Gets {@code length} bytes from this buffer, beginning at {@code offset},
     * and writes them to {@code destination}.
     * <p>
     * The internal write offset of {@code destination} is increased if this
     * operation is successful.
     *
     * @param offset      Offset from beginning of this buffer.
     * @param destination Buffer to write bytes to.
     * @param length      Number of bytes to write.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, length]} is outside
     *                                   the readable range of this buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    default void getAt(final int offset, final BufferWriter destination, final int length) {
        getAt(offset, destination, destination.writeOffset(), length);
        destination.writeOffset(destination.writeOffset() + length);
    }

    /**
     * Gets {@code length} bytes from this buffer, beginning at {@code offset},
     * and copies them to {@code destination} without updating its internal
     * write offset.
     *
     * @param offset            Offset from beginning of this buffer.
     * @param destination       Buffer to write bytes to.
     * @param destinationOffset Offset from beginning of {@code destination}.
     * @param length            Number of bytes to write.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, length]} is outside
     *                                   the readable range of this buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    void getAt(int offset, BufferWriter destination, int destinationOffset, int length);

    /**
     * Gets {@code destination.remaining()} bytes from this buffer, beginning
     * at {@code offset}, and writes them to {@code destination}.
     * <p>
     * The internal write offset, or <i>position</i>, of {@code destination} is
     * increased if this operation is successful.
     *
     * @param offset      Offset from beginning of this buffer.
     * @param destination NIO {@link ByteBuffer} to write bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, destination.remaining()]}
     *                                   is outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    void getAt(int offset, ByteBuffer destination);

    /**
     * Gets four bytes from this buffer at given {@code offset}, which are
     * assumed to contain a IEEE 32-bit binary float with big-endian byte order,
     * and converts them into a Java {@code float}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code float}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default float getF32BeAt(final int offset) {
        return Float.intBitsToFloat(getS32BeAt(offset));
    }

    /**
     * Gets four bytes from this buffer at given {@code offset}, which are
     * assumed to contain a IEEE 32-bit binary float with little-endian
     * byte order, and converts them into a Java {@code float}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code float}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default float getF32LeAt(final int offset) {
        return Float.intBitsToFloat(getS32LeAt(offset));
    }

    /**
     * Gets four bytes from this buffer at given {@code offset}, which are
     * assumed to contain a IEEE 32-bit binary float with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code float}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code float}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default float getF32NeAt(final int offset) {
        return Float.intBitsToFloat(getS32NeAt(offset));
    }

    /**
     * Gets eight bytes from this buffer at given {@code offset}, which are
     * assumed to contain a IEEE 64-bit binary float with big-endian byte order,
     * and converts them into a Java {@code double}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code double}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default double getF64BeAt(final int offset) {
        return Double.longBitsToDouble(getS64BeAt(offset));
    }

    /**
     * Gets eight bytes from this buffer at given {@code offset}, which are
     * assumed to contain a IEEE 64-bit binary float with little-endian
     * byte order, and converts them into a Java {@code double}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code double}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default double getF64LeAt(final int offset) {
        return Double.longBitsToDouble(getS64LeAt(offset));
    }

    /**
     * Gets eight bytes from this buffer at given {@code offset}, which are
     * assumed to contain a IEEE 64-bit binary float with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code double}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code double}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default double getF64NeAt(final int offset) {
        return Double.longBitsToDouble(getS64NeAt(offset));
    }

    /**
     * Gets and returns one byte from this buffer at given {@code offset}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code byte}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 1]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    byte getS8At(int offset);

    /**
     * Gets two bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 16-bit integer with big-endian byte order,
     * and converts them into a Java {@code short}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code short}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    short getS16BeAt(int offset);

    /**
     * Gets two bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 16-bit integer with little-endian byte order,
     * and converts them into a Java {@code short}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code short}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    short getS16LeAt(int offset);

    /**
     * Gets two bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 16-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code short}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code short}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    short getS16NeAt(int offset);

    /**
     * Gets three bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 24-bit integer with big-endian byte order,
     * and converts them into a Java {@code int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int getS24BeAt(int offset) {
        var value = getU24BeAt(offset);
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    /**
     * Gets three bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 24-bit integer with little-endian byte order,
     * and converts them into a Java {@code int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int getS24LeAt(int offset) {
        var value = getU24LeAt(offset);
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    /**
     * Gets three bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 24-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int getS24NeAt(int offset) {
        var value = getU24NeAt(offset);
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    /**
     * Gets four bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 32-bit integer with big-endian byte order,
     * and converts them into a Java {@code int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    int getS32BeAt(int offset);

    /**
     * Gets four bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 32-bit integer with little-endian byte order,
     * and converts them into a Java {@code int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    int getS32LeAt(int offset);

    /**
     * Gets four bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 32-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    int getS32NeAt(int offset);

    /**
     * Gets six bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 48-bit integer with big-endian byte order,
     * and converts them into a Java {@code long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long getS48BeAt(int offset) {
        var value = getU48BeAt(offset);
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }

    /**
     * Gets six bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 48-bit integer with little-endian byte order,
     * and converts them into a Java {@code long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long getS48LeAt(int offset) {
        var value = getU48LeAt(offset);
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;

    }

    /**
     * Gets six bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 48-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long getS48NeAt(int offset) {
        var value = getU48NeAt(offset);
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }

    /**
     * Gets eight bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 64-bit integer with big-endian byte order,
     * and converts them into a Java {@code long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    long getS64BeAt(int offset);

    /**
     * Gets eight bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 64-bit integer with little-endian byte order,
     * and converts them into a Java {@code long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    long getS64LeAt(int offset);

    /**
     * Gets eight bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 64-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    long getS64NeAt(int offset);

    /**
     * Gets and returns one unsigned byte from this buffer at given
     * {@code offset}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned byte as java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 1]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int getU8At(final int offset) {
        return Byte.toUnsignedInt(getS8At(offset));
    }

    /**
     * Gets two bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 16-bit integer with big-endian byte order,
     * and converts them into a Java {@code int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 16-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int getU16BeAt(final int offset) {
        return Short.toUnsignedInt(getS16BeAt(offset));
    }

    /**
     * Gets two bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 16-bit integer with little-endian
     * byte order, and converts them into a Java {@code int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 16-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int getU16LeAt(final int offset) {
        return Short.toUnsignedInt(getS16LeAt(offset));
    }

    /**
     * Gets two bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 16-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 16-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int getU16NeAt(final int offset) {
        return Short.toUnsignedInt(getS16NeAt(offset));
    }

    /**
     * Gets three bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 24-bit integer with big-endian byte order,
     * and converts them into a Java {@code int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 24-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    int getU24BeAt(final int offset);

    /**
     * Gets three bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 24-bit integer with little-endian
     * byte order, and converts them into a Java {@code int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 24-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    int getU24LeAt(final int offset);

    /**
     * Gets three bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 24-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 24-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    int getU24NeAt(final int offset);

    /**
     * Gets four bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 32-bit integer with big-endian byte order,
     * and converts them into a Java {@code long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 32-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long getU32BeAt(final int offset) {
        return Integer.toUnsignedLong(getS32BeAt(offset));
    }

    /**
     * Gets four bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 32-bit integer with little-endian
     * byte order, and converts them into a Java {@code long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 32-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long getU32LeAt(final int offset) {
        return Integer.toUnsignedLong(getS32LeAt(offset));
    }

    /**
     * Gets four bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 32-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 32-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long getU32NeAt(final int offset) {
        return Integer.toUnsignedLong(getS32NeAt(offset));
    }

    /**
     * Gets six bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 48-bit integer with big-endian byte order,
     * and converts them into a Java {@code long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 48-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    long getU48BeAt(final int offset);

    /**
     * Gets six bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 48-bit integer with little-endian
     * byte order, and converts them into a Java {@code long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 48-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    long getU48LeAt(final int offset);

    /**
     * Gets six bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 48-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 48-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    long getU48NeAt(final int offset);

    /**
     * Gets eight bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 64-bit integer with big-endian byte order,
     * and converts them into a Java {@code long}.
     * <p>
     * This method is simply an alias for {@link #getS64BeAt(int)}, as there is
     * no way to represent unsigned 64-bit integers using a primitive type in
     * Java 11.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 64-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long getU64BeAt(final int offset) {
        return getS64BeAt(offset);
    }

    /**
     * Gets eight bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 64-bit integer with little-endian
     * byte order, and converts them into a Java {@code long}.
     * <p>
     * This method is simply an alias for {@link #getS64BeAt(int)}, as there is
     * no way to represent unsigned 64-bit integers using a primitive type in
     * Java 11.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 64-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long getU64LeAt(final int offset) {
        return getS64LeAt(offset);
    }

    /**
     * Gets eight bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 64-bit integer with the same endianess as
     * he system running this application, and converts them into a Java
     * {@code long}.
     * <p>
     * This method is simply an alias for {@link #getS64BeAt(int)}, as there is
     * no way to represent unsigned 64-bit integers using a primitive type in
     * Java 11.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 64-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long getU64NeAt(final int offset) {
        return getS64NeAt(offset);
    }

    /**
     * Copies {@code destination.length} bytes from this buffer, beginning at
     * its internal read offset, to {@code destination}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @param destination Byte array to copy bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, destination.length]}
     *                                   is outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    default void peek(final byte[] destination) {
        peek(destination, 0, destination.length);
    }

    /**
     * Copies {@code length} bytes from this buffer, beginning at its internal
     * read offset, to {@code destination}, beginning at {@code
     * destinationOffset}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @param destination       Byte array to copy bytes to.
     * @param destinationOffset Offset from beginning of {@code destination}.
     * @param length            Number of bytes to copy.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, length]} is outside
     *                                   the readable range of this buffer, or
     *                                   if {@code [destinationOffset, length]}
     *                                   is outside the range of {@code
     *                                   destination}.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    default void peek(final byte[] destination, final int destinationOffset, final int length) {
        getAt(readOffset(), destination, destinationOffset, length);
    }

    /**
     * Gets {@code destination.writableBytes()} bytes from this buffer,
     * beginning at its internal read offset, and writes them to
     * {@code destination}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     * <p>
     * The internal write offset of {@code destination} is increased if this
     * operation is successful.
     *
     * @param destination Buffer to write bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, destination.writableBytes()]}
     *                                   is outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    default void peek(final BufferWriter destination) {
        peek(destination, destination.writableBytes());
    }

    /**
     * Gets {@code length} bytes from this buffer, beginning at its internal
     * read offset, and writes them to {@code destination}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     * <p>
     * The internal write offset of {@code destination} is increased if this
     * operation is successful.
     *
     * @param destination Buffer to write bytes to.
     * @param length      Number of bytes to write.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, length]} is outside
     *                                   the readable range of this buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    default void peek(final BufferWriter destination, final int length) {
        peek(destination, destination.writeOffset(), length);
        destination.writeOffset(destination.writeOffset() + length);
    }

    /**
     * Gets {@code length} bytes from this buffer, beginning at internal read
     * offset, and copies them to {@code destination} without updating its
     * internal write offset.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @param destination       Buffer to write bytes to.
     * @param destinationOffset Offset from beginning of {@code destination}.
     * @param length            Number of bytes to write.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, length]} is outside
     *                                   the readable range of this buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    default void peek(final BufferWriter destination, final int destinationOffset, final int length) {
        getAt(readOffset(), destination, destinationOffset, length);
    }

    /**
     * Gets {@code destination.remaining()} bytes from this buffer, beginning
     * at its internal read offset, and writes them to {@code destination}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     * <p>
     * The internal write offset, or <i>position</i>, of {@code destination} is
     * increased if this operation is successful.
     *
     * @param destination NIO {@link ByteBuffer} to write bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, destination.remaining()]}
     *                                   is outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    default void peek(final ByteBuffer destination) {
        getAt(readOffset(), destination);
    }

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain a IEEE 32-bit binary float with big-endian byte order,
     * and converts them into a Java {@code float}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code float}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default float peekF32Be() {
        return Float.intBitsToFloat(peekS32Be());
    }

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain a IEEE 32-bit binary float with little-endian
     * byte order, and converts them into a Java {@code float}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code float}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default float peekF32Le() {
        return Float.intBitsToFloat(peekS32Le());
    }

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain a IEEE 32-bit binary float with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code float}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code float}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default float peekF32Ne() {
        return Float.intBitsToFloat(peekS32Ne());
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain a IEEE 64-bit binary float with big-endian byte order,
     * and converts them into a Java {@code double}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code double}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default double peekF64Be() {
        return Double.longBitsToDouble(peekS64Be());
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain a IEEE 64-bit binary float with little-endian
     * byte order, and converts them into a Java {@code double}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code double}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default double peekF64Le() {
        return Double.longBitsToDouble(peekS64Le());
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain a IEEE 64-bit binary float with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code double}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code double}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default double peekF64Ne() {
        return Double.longBitsToDouble(peekS64Ne());
    }

    /**
     * Gets and returns one byte from this buffer at its internal read offset.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code byte}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 1]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default byte peekS8() {
        return getS8At(readOffset());
    }

    /**
     * Gets two bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 16-bit integer with big-endian byte order,
     * and converts them into a Java {@code short}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code short}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default short peekS16Be() {
        return getS16BeAt(readOffset());
    }

    /**
     * Gets two bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 16-bit integer with little-endian byte order,
     * and converts them into a Java {@code short}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code short}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default short peekS16Le() {
        return getS16LeAt(readOffset());
    }

    /**
     * Gets two bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 16-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code short}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code short}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default short peekS16Ne() {
        return getS16NeAt(readOffset());
    }

    /**
     * Gets three bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 24-bit integer with big-endian byte order,
     * and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int peekS24Be() {
        return getS24BeAt(readOffset());
    }

    /**
     * Gets three bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 24-bit integer with little-endian byte order,
     * and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int peekS24Le() {
        return getS24LeAt(readOffset());
    }

    /**
     * Gets three bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 24-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code int}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int peekS24Ne() {
        return getS24NeAt(readOffset());
    }

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 32-bit integer with big-endian byte order,
     * and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int peekS32Be() {
        return getS32BeAt(readOffset());
    }

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 32-bit integer with little-endian byte order,
     * and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int peekS32Le() {
        return getS32LeAt(readOffset());
    }

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 32-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code int}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int peekS32Ne() {
        return getS32NeAt(readOffset());
    }

    /**
     * Gets six bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 48-bit integer with big-endian byte order,
     * and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekS48Be() {
        return getS48BeAt(readOffset());
    }

    /**
     * Gets six bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 48-bit integer with little-endian byte order,
     * and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekS48Le() {
        return getS48LeAt(readOffset());
    }

    /**
     * Gets six bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 48-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekS48Ne() {
        return getS48NeAt(readOffset());
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 64-bit integer with big-endian byte order,
     * and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekS64Be() {
        return getS64BeAt(readOffset());
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 64-bit integer with little-endian byte order,
     * and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekS64Le() {
        return getS64LeAt(readOffset());
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 64-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekS64Ne() {
        return getS64NeAt(readOffset());
    }

    /**
     * Gets and returns one unsigned byte from this buffer at its internal read
     * offset.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Unsigned byte as java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 1]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int peekU8() {
        return getU8At(readOffset());
    }

    /**
     * Gets two bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 16-bit integer with big-endian byte order,
     * and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Unsigned 16-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int peekU16Be() {
        return getU16BeAt(readOffset());
    }

    /**
     * Gets two bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 16-bit integer with little-endian
     * byte order, and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Unsigned 16-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int peekU16Le() {
        return getU16LeAt(readOffset());
    }

    /**
     * Gets two bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 16-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code int}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Unsigned 16-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int peekU16Ne() {
        return getU16NeAt(readOffset());
    }

    /**
     * Gets three bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 24-bit integer with big-endian byte order,
     * and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Unsigned 24-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int peekU24Be() {
        return getU24BeAt(readOffset());
    }

    /**
     * Gets three bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 24-bit integer with little-endian
     * byte order, and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Unsigned 24-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int peekU24Le() {
        return getU24LeAt(readOffset());
    }

    /**
     * Gets three bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 24-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code int}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Unsigned 24-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int peekU24Ne() {
        return getU24NeAt(readOffset());
    }

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 32-bit integer with big-endian byte order,
     * and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Unsigned 32-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekU32Be() {
        return getU32BeAt(readOffset());
    }

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 32-bit integer with little-endian
     * byte order, and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Unsigned 32-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekU32Le() {
        return getU32LeAt(readOffset());
    }

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 32-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Unsigned 32-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekU32Ne() {
        return getU32NeAt(readOffset());
    }

    /**
     * Gets six bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 48-bit integer with big-endian byte
     * order, and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Unsigned 48-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekU48Be() {
        return getU48BeAt(readOffset());
    }

    /**
     * Gets six bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 48-bit integer with little-endian byte
     * order, and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Unsigned 48-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekU48Le() {
        return getU48LeAt(readOffset());
    }

    /**
     * Gets six bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 48-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     *
     * @return Unsigned 48-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekU48Ne() {
        return getU48NeAt(readOffset());
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 64-bit integer with big-endian byte order,
     * and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     * <p>
     * This method is simply an alias for {@link #peekS64Be()}, as there is no
     * way to represent unsigned 64-bit integers using a primitive type in Java
     * 11.
     *
     * @return Unsigned 64-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekU64Be() {
        return peekS64Be();
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 64-bit integer with little-endian byte
     * order, and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     * <p>
     * This method is simply an alias for {@link #peekS64Le()}, as there is no
     * way to represent unsigned 64-bit integers using a primitive type in Java
     * 11.
     *
     * @return Unsigned 64-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekU64Le() {
        return peekS64Le();
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 64-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code long}.
     * <p>
     * The internal read offset of this buffer remains unchanged by this
     * operation.
     * <p>
     * This method is simply an alias for {@link #peekS64Le()}, as there is no
     * way to represent unsigned 64-bit integers using a primitive type in Java
     * 11.
     *
     * @return Unsigned 64-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long peekU64Ne() {
        return peekS64Ne();
    }

    /**
     * Gets the number of bytes that can currently be read from this buffer.
     *
     * @return Current number of readable bytes.
     * @throws BufferIsClosed If this buffer is closed, this exception may be
     *                        thrown.
     */
    int readableBytes();

    /**
     * Gets copy of internal read offset, which determines from where the next
     * byte will be read.
     *
     * @return Copy of internal read offset.
     * @throws BufferIsClosed If this buffer is closed, this exception may be
     *                        thrown.
     */
    int readOffset();

    /**
     * Updates the internal read offset by setting it to the given value.
     *
     * @param readOffset Desired new internal read offset.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code readOffset < 0 ||
     *                                   readEnd() < readOffset}.
     */
    void readOffset(int readOffset);

    /**
     * Gets position of the last readable byte in this buffer.
     *
     * @return Offset of last readable byte in this buffer.
     * @throws BufferIsClosed If this buffer is closed, this exception may be
     *                        thrown.
     */
    int readEnd();

    /**
     * Copies {@code destination.length} bytes from this buffer, beginning at
     * its internal read offset, to {@code destination}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @param destination Byte array to copy bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, destination.length]}
     *                                   is outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    default void read(final byte[] destination) {
        read(destination, 0, destination.length);
    }

    /**
     * Copies {@code length} bytes from this buffer, beginning at its internal
     * read offset, to {@code destination}, beginning at {@code
     * destinationOffset}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @param destination       Byte array to copy bytes to.
     * @param destinationOffset Offset from beginning of {@code destination}.
     * @param length            Number of bytes to copy.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, length]} is outside
     *                                   the readable range of this buffer, or
     *                                   if {@code [destinationOffset, length]}
     *                                   is outside the range of {@code
     *                                   destination}.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    void read(byte[] destination, int destinationOffset, int length);

    /**
     * Gets {@code destination.writableBytes()} bytes from this buffer,
     * beginning at its internal read offset, and writes them to
     * {@code destination}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     * <p>
     * The internal write offset of {@code destination} is increased if this
     * operation is successful.
     *
     * @param destination Buffer to write bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, destination.writableBytes()]}
     *                                   is outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    default void read(final BufferWriter destination) {
        read(destination, destination.writableBytes());
    }

    /**
     * Gets {@code length} bytes from this buffer, beginning at its internal
     * read offset, and writes them to {@code destination}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     * <p>
     * The internal write offset of {@code destination} is increased if this
     * operation is successful.
     *
     * @param destination Buffer to write bytes to.
     * @param length      Number of bytes to write.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, length]} is outside
     *                                   the readable range of this buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    default void read(final BufferWriter destination, final int length) {
        read(destination, destination.writeOffset(), length);
        destination.writeOffset(destination.writeOffset() + length);
    }

    /**
     * Gets {@code length} bytes from this buffer, beginning at internal read
     * offset, and copies them to {@code destination} without updating its
     * internal write offset.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @param destination       Buffer to write bytes to.
     * @param destinationOffset Offset from beginning of {@code destination}.
     * @param length            Number of bytes to write.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, length]} is outside
     *                                   the readable range of this buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    void read(BufferWriter destination, int destinationOffset, int length);

    /**
     * Gets {@code destination.remaining()} bytes from this buffer, beginning
     * at its internal read offset, and writes them to {@code destination}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     * <p>
     * The internal write offset, or <i>position</i>, of {@code destination} is
     * increased if this operation is successful.
     *
     * @param destination NIO {@link ByteBuffer} to write bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, destination.remaining()]}
     *                                   is outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code null}.
     */
    void read(ByteBuffer destination);

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain a IEEE 32-bit binary float with big-endian byte order,
     * and converts them into a Java {@code float}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code float}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default float readF32Be() {
        return Float.intBitsToFloat(readS32Be());
    }

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain a IEEE 32-bit binary float with little-endian
     * byte order, and converts them into a Java {@code float}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code float}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default float readF32Le() {
        return Float.intBitsToFloat(readS32Le());
    }

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain a IEEE 32-bit binary float with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code float}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code float}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default float readF32Ne() {
        return Float.intBitsToFloat(readS32Ne());
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain a IEEE 64-bit binary float with big-endian byte order,
     * and converts them into a Java {@code double}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code double}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default double readF64Be() {
        return Double.longBitsToDouble(readS64Be());
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain a IEEE 64-bit binary float with little-endian
     * byte order, and converts them into a Java {@code double}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code double}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default double readF64Le() {
        return Double.longBitsToDouble(readS64Le());
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain a IEEE 64-bit binary float with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code double}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code double}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default double readF64Ne() {
        return Double.longBitsToDouble(readS64Ne());
    }

    /**
     * Gets and returns one byte from this buffer at its internal read offset.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code byte}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 1]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    byte readS8();

    /**
     * Gets two bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 16-bit integer with big-endian byte order,
     * and converts them into a Java {@code short}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code short}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    short readS16Be();

    /**
     * Gets two bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 16-bit integer with little-endian byte order,
     * and converts them into a Java {@code short}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code short}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    short readS16Le();

    /**
     * Gets two bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 16-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code short}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code short}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    short readS16Ne();

    /**
     * Gets three bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 24-bit integer with big-endian byte order,
     * and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int readS24Be() {
        var value = readU24Be();
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    /**
     * Gets three bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 24-bit integer with little-endian byte order,
     * and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int readS24Le() {
        var value = readU24Le();
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }


    /**
     * Gets three bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 24-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code int}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int readS24Ne() {
        var value = readU24Ne();
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 32-bit integer with big-endian byte order,
     * and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    int readS32Be();

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 32-bit integer with little-endian byte order,
     * and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    int readS32Le();

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 32-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code int}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    int readS32Ne();

    /**
     * Gets six bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 48-bit integer with big-endian byte order,
     * and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long readS48Be() {
        var value = readU48Be();
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }

    /**
     * Gets six bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 48-bit integer with little-endian byte order,
     * and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long readS48Le() {
        var value = readU48Le();
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }


    /**
     * Gets six bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 48-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long readS48Ne() {
        var value = readU48Ne();
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 64-bit integer with big-endian byte order,
     * and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    long readS64Be();


    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 64-bit integer with little-endian byte order,
     * and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    long readS64Le();


    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain a signed 64-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    long readS64Ne();


    /**
     * Gets and returns one unsigned byte from this buffer at its internal read
     * offset.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Unsigned byte as java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 1]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int readU8() {
        return Byte.toUnsignedInt(readS8());
    }

    /**
     * Gets two bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 16-bit integer with big-endian byte order,
     * and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 16-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int readU16Be() {
        return Short.toUnsignedInt(readS16Be());
    }

    /**
     * Gets two bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 16-bit integer with little-endian
     * byte order, and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 16-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int readU16Le() {
        return Short.toUnsignedInt(readS16Le());
    }

    /**
     * Gets two bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 16-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code int}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 16-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int readU16Ne() {
        return Short.toUnsignedInt(readS16Ne());
    }

    /**
     * Gets three bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 24-bit integer with big-endian byte order,
     * and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 24-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    int readU24Be();

    /**
     * Gets three bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 24-bit integer with little-endian
     * byte order, and converts them into a Java {@code int}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 24-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    int readU24Le();

    /**
     * Gets three bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 24-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code int}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 24-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    int readU24Ne();

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 32-bit integer with big-endian byte order,
     * and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 32-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long readU32Be() {
        return Integer.toUnsignedLong(readS32Be());
    }

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 32-bit integer with little-endian
     * byte order, and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 32-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long readU32Le() {
        return Integer.toUnsignedLong(readS32Le());
    }

    /**
     * Gets four bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 32-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 32-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long readU32Ne() {
        return Integer.toUnsignedLong(readS32Ne());
    }

    /**
     * Gets six bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 48-bit integer with big-endian byte
     * order, and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 48-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    long readU48Be();

    /**
     * Gets six bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 48-bit integer with little-endian byte
     * order, and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 48-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    long readU48Le();

    /**
     * Gets six bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 48-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 48-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    long readU48Ne();

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 64-bit integer with big-endian byte order,
     * and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     * <p>
     * This method is simply an alias for {@link #peekS64Be()}, as there is no
     * way to represent unsigned 64-bit integers using a primitive type in Java
     * 11.
     *
     * @return Unsigned 64-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long readU64Be() {
        return readS64Be();
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 64-bit integer with little-endian byte
     * order, and converts them into a Java {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     * <p>
     * This method is simply an alias for {@link #peekS64Le()}, as there is no
     * way to represent unsigned 64-bit integers using a primitive type in Java
     * 11.
     *
     * @return Unsigned 64-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long readU64Le() {
        return readS64Le();
    }

    /**
     * Gets eight bytes from this buffer at its internal read offset, which are
     * assumed to contain an unsigned 64-bit integer with the same endianess as
     * the system running this application, and converts them into a Java
     * {@code long}.
     * <p>
     * The internal read offset of this buffer is updated by this operation.
     * <p>
     * This method is simply an alias for {@link #peekS64Le()}, as there is no
     * way to represent unsigned 64-bit integers using a primitive type in Java
     * 11.
     *
     * @return Unsigned 64-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long readU64Ne() {
        return readS64Ne();
    }

    /**
     * Increases the internal read offset of this buffer with {@code length}
     * bytes.
     *
     * @param length Number of bytes to skip reading from this buffer.
     * @throws BufferIsClosed If
     */
    void skip(int length);
}
