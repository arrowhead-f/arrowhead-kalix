package se.arkalix.io.buf;

import se.arkalix.io.IoException;
import se.arkalix.util._internal.BinaryMath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;

/**
 * A collection of memory that can be read from.
 * <p>
 * Keeps track of a {@link #readOffset() read offset}, which is increased
 * automatically whenever a read operation is successfully performed on this
 * buffer. It also keeps tack of a {@link #readEnd() read end}, which may never
 * be passed by the read offset.
 * <p>
 * The below diagram illustrates how this works in practice. The buffer consists
 * of a sequence of bytes, denoted by squares in the diagram. Each byte has an
 * <i>offset</i> relative to the beginning of the memory, as well as a current
 * <i>value</i>. The read offset and end both point to offsets within the
 * buffer. Whenever a byte is read from the buffer, the read offset is moved
 * closer to the end offset. If the buffer is written to through some other
 * class or interface, the read end may move further away from the read offset.
 * <pre>
 *   Offset: 0   1   2   3   4   5   6   7   ...
 *           +---+---+---+---+---+---+---+---+-----+
 *    Value: | 0 | 0 | 5 | 1 | 9 | 0 | 1 | 0 | ... |
 *           +---+---+---+---+---+---+---+---+-----+
 *               A               A
 *               |               |
 *          Read Offset       Read End
 * </pre>
 * To support getting the contents of the buffer without its read offset being
 * updated, this interface also provides numerous methods prefixed with {@code
 * get} and {@code peek}. The former category of methods read bytes from an
 * explicitly specified read offset, while the latter uses the read offset
 * without updating it.
 *
 * @see Buffer
 * @see BufferWriter
 */
@SuppressWarnings("unused")
public interface BufferReader extends AutoCloseable {
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
     * @throws IndexOutOfBoundsException If {@code [offset, offset +
     *                                   destination.length]} is outside the
     *                                   readable range of this buffer.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
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
     * @throws IndexOutOfBoundsException If {@code [offset, offset + length]} is
     *                                   outside the readable range of this
     *                                   buffer, or if {@code [destinationOffset,
     *                                   destinationOffset + length]} is outside
     *                                   the range of {@code destination}.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    default void getAt(final int offset, final byte[] destination, final int destinationOffset, final int length) {
        try (final var destinationBuffer = Buffer.wrap(destination)) {
            destinationBuffer.clear();
            getAt(offset, destinationBuffer, destinationOffset, length);
        }
    }

    /**
     * Gets {@code destination.writableBytes()} bytes from this buffer,
     * beginning at {@code offset}, and writes them to {@code destination}.
     * <p>
     * The write offset of {@code destination} is increased if this operation is
     * successful.
     *
     * @param offset      Offset from beginning of this buffer.
     * @param destination Buffer to write bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, offset +
     *                                   destination.writableBytes()]} is
     *                                   outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    default void getAt(final int offset, final BufferWriter destination) {
        getAt(offset, destination, destination.writableBytes());
    }

    /**
     * Gets {@code length} bytes from this buffer, beginning at {@code offset},
     * and writes them to {@code destination}.
     * <p>
     * The write offset of {@code destination} is increased if this operation is
     * successful.
     *
     * @param offset      Offset from beginning of this buffer.
     * @param destination Buffer to write bytes to.
     * @param length      Number of bytes to write.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, offset + length]} is
     *                                   outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    default void getAt(final int offset, final BufferWriter destination, final int length) {
        getAt(offset, destination, destination.writeOffset(), length);
        destination.writeOffset(destination.writeOffset() + length);
    }

    /**
     * Gets {@code length} bytes from this buffer, beginning at {@code offset},
     * and copies them to {@code destination} without updating its write
     * offset.
     *
     * @param offset            Offset from beginning of this buffer.
     * @param destination       Buffer to write bytes to.
     * @param destinationOffset Offset from beginning of {@code destination}.
     * @param length            Number of bytes to write.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, offset + length]} is
     *                                   outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    void getAt(int offset, BufferWriter destination, int destinationOffset, int length);

    /**
     * Gets {@code destination.remaining()} bytes from this buffer, beginning at
     * {@code offset}, and writes them to {@code destination}.
     * <p>
     * The write offset, or <i>position</i>, of {@code destination} is increased
     * if this operation is successful.
     *
     * @param offset      Offset from beginning of this buffer.
     * @param destination NIO {@link ByteBuffer} to write bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, offset +
     *                                   destination.remaining()]} is outside
     *                                   the readable range of this buffer.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    void getAt(int offset, ByteBuffer destination);

    /**
     * Gets as many bytes as possible from this buffer, beginning at {@code
     * offset}, and writes them to {@code destination}.
     * <p>
     * The number of bytes written by this operation is returned upon
     * completion.
     *
     * @param offset      Offset from beginning of this buffer.
     * @param destination NIO {@link java.nio.channels.Channel Channel} to write
     *                    bytes to.
     * @return Number of bytes written to {@code destination}. Some
     * implementations may return an integer equal to or less than zero to hint
     * that the {@code destination} has been closed. Others may instead throw an
     * exception to signify the same occurrence.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code offset} is outside the
     *                                   readable range of this buffer.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    default int getAt(final int offset, final WritableByteChannel destination) {
        return getAt(offset, destination, readableBytes());
    }

    /**
     * Gets no more than {@code maxLength} bytes from this buffer, beginning at
     * {@code offset}, and writes them to {@code destination}.
     * <p>
     * How many bytes are written depends both on how many bytes are available
     * for reading at the given {@code offset} and how many bytes the {@code
     * destination} is ready to receive. The number of bytes written by this
     * operation is returned when it completes.
     *
     * @param offset      Offset from beginning of this buffer.
     * @param destination NIO {@link java.nio.channels.Channel Channel} to write
     *                    bytes to.
     * @param maxLength   Maximum number of bytes to write to {@code
     *                    destination}.
     * @return Number of bytes written to {@code destination}. Some
     * implementations may return an integer equal to or less than zero to hint
     * that the {@code destination} has been closed. Others may instead throw an
     * exception to signify the same occurrence.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code offset} is outside the
     *                                   readable range of this buffer or {@code
     *                                   maxLength < 0}.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     * @throws IoException               If some other I/O-related issue
     *                                   occurs.
     */
    int getAt(int offset, WritableByteChannel destination, int maxLength);

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
     * assumed to contain a IEEE 32-bit binary float with little-endian byte
     * order, and converts them into a Java {@code float}.
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
     * the system running this application, and converts them into a Java {@code
     * float}.
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
     * assumed to contain a IEEE 64-bit binary float with little-endian byte
     * order, and converts them into a Java {@code double}.
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
     * the system running this application, and converts them into a Java {@code
     * double}.
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
    default byte getS8At(final int offset) {
        final var buffer = new byte[1];
        getAt(offset, buffer);
        return buffer[0];
    }

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
    default short getS16BeAt(final int offset) {
        final var buffer = new byte[2];
        getAt(offset, buffer);
        return BinaryMath.getS16BeAt(buffer, 0);
    }

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
    default short getS16LeAt(final int offset) {
        final var buffer = new byte[2];
        getAt(offset, buffer);
        return BinaryMath.getS16LeAt(buffer, 0);
    }

    /**
     * Gets two bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 16-bit integer with the same endianess as the
     * system running this application, and converts them into a Java {@code
     * short}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code short}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 2]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default short getS16NeAt(int offset) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return getS16BeAt(offset);
        }
        else {
            return getS16LeAt(offset);
        }
    }

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
     * assumed to contain a signed 24-bit integer with the same endianess as the
     * system running this application, and converts them into a Java {@code
     * int}.
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
    default int getS32BeAt(final int offset) {
        final var buffer = new byte[4];
        getAt(offset, buffer);
        return BinaryMath.getS32BeAt(buffer, 0);
    }

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
    default int getS32LeAt(final int offset) {
        final var buffer = new byte[4];
        getAt(offset, buffer);
        return BinaryMath.getS32LeAt(buffer, 0);
    }

    /**
     * Gets four bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 32-bit integer with the same endianess as the
     * system running this application, and converts them into a Java {@code
     * int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 4]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int getS32NeAt(int offset) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return getS32BeAt(offset);
        }
        else {
            return getS32LeAt(offset);
        }
    }

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
     * assumed to contain a signed 48-bit integer with the same endianess as the
     * system running this application, and converts them into a Java {@code
     * long}.
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
    default long getS64BeAt(final int offset) {
        final var buffer = new byte[8];
        getAt(offset, buffer);
        return BinaryMath.getS64BeAt(buffer, 0);
    }

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
    default long getS64LeAt(final int offset) {
        final var buffer = new byte[8];
        getAt(offset, buffer);
        return BinaryMath.getS64LeAt(buffer, 0);
    }

    /**
     * Gets eight bytes from this buffer at given {@code offset}, which are
     * assumed to contain a signed 64-bit integer with the same endianess as the
     * system running this application, and converts them into a Java {@code
     * long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 8]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long getS64NeAt(int offset) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return getS64BeAt(offset);
        }
        else {
            return getS64LeAt(offset);
        }
    }

    /**
     * Gets and returns one unsigned byte from this buffer at given {@code
     * offset}.
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
     * assumed to contain an unsigned 16-bit integer with little-endian byte
     * order, and converts them into a Java {@code int}.
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
     * the system running this application, and converts them into a Java {@code
     * int}.
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
    default int getU24BeAt(final int offset) {
        final var buffer = new byte[3];
        getAt(offset, buffer);
        return BinaryMath.getU24BeAt(buffer, 0);
    }

    /**
     * Gets three bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 24-bit integer with little-endian byte
     * order, and converts them into a Java {@code int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 24-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int getU24LeAt(final int offset) {
        final var buffer = new byte[3];
        getAt(offset, buffer);
        return BinaryMath.getU24LeAt(buffer, 0);
    }

    /**
     * Gets three bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 24-bit integer with the same endianess as
     * the system running this application, and converts them into a Java {@code
     * int}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 24-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 3]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default int getU24NeAt(final int offset) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return getU24BeAt(offset);
        }
        else {
            return getU24LeAt(offset);
        }
    }

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
     * assumed to contain an unsigned 32-bit integer with little-endian byte
     * order, and converts them into a Java {@code long}.
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
     * the system running this application, and converts them into a Java {@code
     * long}.
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
    default long getU48BeAt(final int offset) {
        final var buffer = new byte[6];
        getAt(offset, buffer);
        return BinaryMath.getU48BeAt(buffer, 0);
    }

    /**
     * Gets six bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 48-bit integer with little-endian byte
     * order, and converts them into a Java {@code long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 48-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long getU48LeAt(final int offset) {
        final var buffer = new byte[6];
        getAt(offset, buffer);
        return BinaryMath.getU48LeAt(buffer, 0);
    }

    /**
     * Gets six bytes from this buffer at given {@code offset}, which are
     * assumed to contain an unsigned 48-bit integer with the same endianess as
     * the system running this application, and converts them into a Java {@code
     * long}.
     *
     * @param offset Offset from beginning of this buffer.
     * @return Unsigned 48-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[offset, offset + 6]}} is
     *                                   outside the readable range of this
     *                                   buffer.
     */
    default long getU48NeAt(final int offset) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return getU48BeAt(offset);
        }
        else {
            return getU48LeAt(offset);
        }
    }

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
     * assumed to contain an unsigned 64-bit integer with little-endian byte
     * order, and converts them into a Java {@code long}.
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
     * he system running this application, and converts them into a Java {@code
     * long}.
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
     * its read offset, to {@code destination}.
     * <p>
     * The read offset of this buffer remains unchanged by this operation.
     *
     * @param destination Byte array to copy bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, offset +
     *                                   destination.length]} is outside the
     *                                   readable range of this buffer.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    default void peek(final byte[] destination) {
        peek(destination, 0, destination.length);
    }

    /**
     * Copies {@code length} bytes from this buffer, beginning at its read
     * offset, to {@code destination}, beginning at {@code destinationOffset}.
     * <p>
     * The read offset of this buffer remains unchanged by this operation.
     *
     * @param destination       Byte array to copy bytes to.
     * @param destinationOffset Offset from beginning of {@code destination}.
     * @param length            Number of bytes to copy.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, offset + length]} is
     *                                   outside the readable range of this
     *                                   buffer, or if {@code [destinationOffset,
     *                                   destinationOffset + length]} is outside
     *                                   the range of {@code destination}.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    default void peek(final byte[] destination, final int destinationOffset, final int length) {
        getAt(readOffset(), destination, destinationOffset, length);
    }

    /**
     * Gets {@code destination.writableBytes()} bytes from this buffer,
     * beginning at its read offset, and writes them to {@code destination}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
     * <p>
     * The write offset of {@code destination} is increased if this operation is
     * successful.
     *
     * @param destination Buffer to write bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, offset +
     *                                   destination.writableBytes()]} is
     *                                   outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    default void peek(final BufferWriter destination) {
        peek(destination, destination.writableBytes());
    }

    /**
     * Gets {@code length} bytes from this buffer, beginning at its read offset,
     * and writes them to {@code destination}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
     * <p>
     * The write offset of {@code destination} is increased if this operation is
     * successful.
     *
     * @param destination Buffer to write bytes to.
     * @param length      Number of bytes to write.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, offset + length]} is
     *                                   outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    default void peek(final BufferWriter destination, final int length) {
        peek(destination, destination.writeOffset(), length);
        destination.writeOffset(destination.writeOffset() + length);
    }

    /**
     * Gets {@code length} bytes from this buffer, beginning at read offset, and
     * copies them to {@code destination} without updating its write offset.
     * <p>
     * The read offset of this buffer is not changed by this operation.
     *
     * @param destination       Buffer to write bytes to.
     * @param destinationOffset Offset from beginning of {@code destination}.
     * @param length            Number of bytes to write.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, offset + length]} is
     *                                   outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    default void peek(final BufferWriter destination, final int destinationOffset, final int length) {
        getAt(readOffset(), destination, destinationOffset, length);
    }

    /**
     * Gets {@code destination.remaining()} bytes from this buffer, beginning at
     * its read offset, and writes them to {@code destination}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
     * <p>
     * The write offset, or <i>position</i>, of {@code destination} is increased
     * if this operation is successful.
     *
     * @param destination NIO {@link ByteBuffer} to write bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [offset, offset +
     *                                   destination.remaining()]} is outside
     *                                   the readable range of this buffer.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    default void peek(final ByteBuffer destination) {
        getAt(readOffset(), destination);
    }

    /**
     * Gets as many bytes as possible from this buffer, beginning at its read
     * offset, and writes them to {@code destination}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
     * <p>
     * How many bytes are written depends both on how many bytes are available
     * for reading at the current {@link #readOffset() read offset} and how many
     * bytes the {@code destination} is ready to receive. The number of bytes
     * written by this operation is returned when it completes.
     *
     * @param destination NIO {@link java.nio.channels.Channel Channel} to write
     *                    bytes to.
     * @return Number of bytes written to {@code destination}. Some
     * implementations may return an integer equal to or less than zero to hint
     * that the {@code destination} has been closed. Others may instead throw an
     * exception to signify the same occurrence.
     * @throws BufferIsClosed       If this buffer is closed.
     * @throws NullPointerException If {@code destination} is {@code null}.
     * @throws IoException          If some other I/O-related issue occurs.
     */
    default int peek(final WritableByteChannel destination) {
        return getAt(readOffset(), destination, readableBytes());
    }

    /**
     * Gets no more than {@code limit} bytes from this buffer, beginning at its
     * read offset, and writes them to {@code destination}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
     * <p>
     * The number of bytes written by this operation is returned upon
     * completion.
     *
     * @param destination NIO {@link java.nio.channels.Channel Channel} to write
     *                    bytes to.
     * @param maxLength   Maximum number of bytes to write to {@code
     *                    destination}.
     * @return Number of bytes written to {@code destination}. Some
     * implementations may return an integer equal to or less than zero to hint
     * that the {@code destination} has been closed. Others may instead throw an
     * exception to signify the same occurrence.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code maxLength < 0}.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     * @throws IoException               If some other I/O-related issue
     *                                   occurs.
     */
    default int peek(final WritableByteChannel destination, final int maxLength) {
        return getAt(readOffset(), destination, maxLength);
    }

    /**
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain a IEEE 32-bit binary float with big-endian byte order, and
     * converts them into a Java {@code float}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain a IEEE 32-bit binary float with little-endian byte order, and
     * converts them into a Java {@code float}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain a IEEE 32-bit binary float with the same endianess as the system
     * running this application, and converts them into a Java {@code float}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain a IEEE 64-bit binary float with big-endian byte order, and
     * converts them into a Java {@code double}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain a IEEE 64-bit binary float with little-endian byte order, and
     * converts them into a Java {@code double}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain a IEEE 64-bit binary float with the same endianess as the
     * system running this application, and converts them into a Java {@code
     * double}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets and returns one byte from this buffer at its read offset.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets two bytes from this buffer at its read offset, which are assumed to
     * contain a signed 16-bit integer with big-endian byte order, and converts
     * them into a Java {@code short}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets two bytes from this buffer at its read offset, which are assumed to
     * contain a signed 16-bit integer with little-endian byte order, and
     * converts them into a Java {@code short}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets two bytes from this buffer at its read offset, which are assumed to
     * contain a signed 16-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code short}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets three bytes from this buffer at its read offset, which are assumed
     * to contain a signed 24-bit integer with big-endian byte order, and
     * converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets three bytes from this buffer at its read offset, which are assumed
     * to contain a signed 24-bit integer with little-endian byte order, and
     * converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets three bytes from this buffer at its read offset, which are assumed
     * to contain a signed 24-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain a signed 32-bit integer with big-endian byte order, and converts
     * them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain a signed 32-bit integer with little-endian byte order, and
     * converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain a signed 32-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets six bytes from this buffer at its read offset, which are assumed to
     * contain a signed 48-bit integer with big-endian byte order, and converts
     * them into a Java {@code big-endian}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets six bytes from this buffer at its read offset, which are assumed to
     * contain a signed 48-bit integer with little-endian byte order, and
     * converts them into a Java {@code little-endian}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets six bytes from this buffer at its read offset, which are assumed to
     * contain a signed 48-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain a signed 64-bit integer with big-endian byte order, and
     * converts them into a Java {@code big-endian}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain a signed 64-bit integer with little-endian byte order, and
     * converts them into a Java {@code little-endian}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain a signed 64-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets and returns one unsigned byte from this buffer at its read offset.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets two bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 16-bit integer with big-endian byte order, and
     * converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets two bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 16-bit integer with little-endian byte order, and
     * converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets two bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 16-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets three bytes from this buffer at its read offset, which are assumed
     * to contain an unsigned 24-bit integer with big-endian byte order, and
     * converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets three bytes from this buffer at its read offset, which are assumed
     * to contain an unsigned 24-bit integer with little-endian byte order, and
     * converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets three bytes from this buffer at its read offset, which are assumed
     * to contain an unsigned 24-bit integer with the same endianess as the
     * system running this application, and converts them into a Java {@code
     * int}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 32-bit integer with big-endian byte order, and
     * converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 32-bit integer with little-endian byte order, and
     * converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 32-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets six bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 48-bit integer with big-endian byte order, and
     * converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets six bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 48-bit integer with little-endian byte order, and
     * converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets six bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 48-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain an unsigned 64-bit integer with big-endian byte order, and
     * converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain an unsigned 64-bit integer with little-endian byte order, and
     * converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain an unsigned 64-bit integer with the same endianess as the
     * system running this application, and converts them into a Java {@code
     * long}.
     * <p>
     * The read offset of this buffer is not changed by this operation.
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
     * @throws BufferIsClosed If this buffer is closed. Not guaranteed to be
     *                        thrown by all implementations.
     */
    default int readableBytes() {
        return readEnd() - readOffset();
    }

    /**
     * Gets copy of current read offset, which points to the next byte in this
     * buffer that will be read by the any subsequent read operation.
     *
     * @return Copy of read offset.
     * @throws BufferIsClosed If this buffer is closed. Not guaranteed to be
     *                        thrown by all implementations.
     */
    int readOffset();

    /**
     * Updates the read offset by setting it to the given value.
     *
     * @param readOffset Desired new read offset.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code readOffset < 0 || readOffset
     *                                   > readEnd()}.
     */
    void readOffset(int readOffset);

    /**
     * Gets position of the last readable byte in this buffer.
     *
     * @return Offset of last readable byte in this buffer.
     * @throws BufferIsClosed If this buffer is closed. Not guaranteed to be
     *                        thrown by all implementations.
     */
    int readEnd();

    /**
     * Copies {@code destination.length} bytes from this buffer, beginning at
     * its read offset, to {@code destination}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @param destination Byte array to copy bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [readOffset(), readOffset() +
     *                                   destination.length]} is outside the
     *                                   readable range of this buffer.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    default void read(final byte[] destination) {
        read(destination, 0, destination.length);
    }

    /**
     * Copies {@code length} bytes from this buffer, beginning at its read
     * offset, to {@code destination}, beginning at {@code destinationOffset}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @param destination       Byte array to copy bytes to.
     * @param destinationOffset Offset from beginning of {@code destination}.
     * @param length            Number of bytes to copy.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [readOffset(), readOffset() +
     *                                   length]} is outside the readable range
     *                                   of this buffer, or if {@code
     *                                   [destinationOffset, destinationOffset +
     *                                   length]} is outside the range of {@code
     *                                   destination}.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    default void read(final byte[] destination, final int destinationOffset, final int length) {
        try (final var buffer = Buffer.wrap(destination)) {
            buffer.clear();
            read(buffer, destinationOffset, length);
        }
    }

    /**
     * Gets {@code destination.writableBytes()} bytes from this buffer,
     * beginning at its read offset, and writes them to {@code destination}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     * <p>
     * The write offset of {@code destination} is increased if this operation is
     * successful.
     *
     * @param destination Buffer to write bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [readOffset(), readOffset() +
     *                                   destination.writableBytes()]} is
     *                                   outside the readable range of this
     *                                   buffer.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    default void read(final BufferWriter destination) {
        read(destination, destination.writableBytes());
    }

    /**
     * Gets {@code length} bytes from this buffer, beginning at its read offset,
     * and writes them to {@code destination}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     * <p>
     * The write offset of {@code destination} is increased if this operation is
     * successful.
     *
     * @param destination Buffer to write bytes to.
     * @param length      Number of bytes to write.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [readOffset(), readOffset() +
     *                                   length]} is outside the readable range
     *                                   of this buffer, or if there are less
     *                                   than {@code length} bytes remaining in
     *                                   {@code destination}.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    default void read(final BufferWriter destination, final int length) {
        read(destination, destination.writeOffset(), length);
        destination.writeOffset(destination.writeOffset() + length);
    }

    /**
     * Gets {@code length} bytes from this buffer, beginning at read offset, and
     * copies them to {@code destination} without updating its write offset.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @param destination       Buffer to write bytes to.
     * @param destinationOffset Offset from beginning of {@code destination}.
     * @param length            Number of bytes to write.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [readOffset(), readOffset() +
     *                                   length]} is outside the readable range
     *                                   of this buffer, or if {@code
     *                                   [destinationOffset, destinationOffset +
     *                                   length]} is outside the writable range
     *                                   of {@code destination}.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    void read(BufferWriter destination, int destinationOffset, int length);

    /**
     * Reads up to {@code destination.remaining()} bytes from this buffer and
     * writes them to {@code destination}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     * <p>
     * The write offset, or <i>position</i>, of {@code destination} is increased
     * if this operation is successful.
     *
     * @param destination NIO {@link ByteBuffer} to write bytes to.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code [readOffset(), readOffset() +
     *                                   destination.remaining()]} is outside
     *                                   the readable range of this buffer.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     */
    void read(ByteBuffer destination);

    /**
     * Gets as many bytes as possible from this buffer, beginning at its read
     * offset, and writes them to {@code destination}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     * <p>
     * The number of bytes written by this operation is returned upon
     * completion.
     *
     * @param destination NIO {@link java.nio.channels.Channel Channel} to write
     *                    bytes to.
     * @return Number of bytes written to {@code destination}. Some
     * implementations may return an integer equal to or less than zero to hint
     * that the {@code destination} has been closed. Others may instead throw an
     * exception to signify the same occurrence.
     * @throws BufferIsClosed       If this buffer is closed.
     * @throws NullPointerException If {@code destination} is {@code null}.
     * @throws IoException          If some other I/O-related issue occurs.
     */
    default int read(final WritableByteChannel destination) {
        return read(destination, readableBytes());
    }

    /**
     * Gets no more than {@code limit} bytes from this buffer, beginning at its
     * read offset, and writes them to {@code destination}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     * <p>
     * How many bytes are written depends both on how many bytes are available
     * for reading at the current {@link #readOffset() read offset} and how many
     * bytes the {@code destination} is ready to receive. The number of bytes
     * written by this operation is returned when it completes.
     *
     * @param destination NIO {@link java.nio.channels.Channel Channel} to write
     *                    bytes to.
     * @param maxLength   Maximum number of bytes to write to {@code
     *                    destination}.
     * @return Number of bytes written to {@code destination}. Some
     * implementations may return an integer equal to or less than zero to hint
     * that the {@code destination} has been closed. Others may instead throw an
     * exception to signify the same occurrence.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code maxLength < 0}.
     * @throws NullPointerException      If {@code destination} is {@code
     *                                   null}.
     * @throws IoException               If some other I/O-related issue
     *                                   occurs.
     */
    int read(WritableByteChannel destination, int maxLength);

    /**
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain a IEEE 32-bit binary float with big-endian byte order, and
     * converts them into a Java {@code float}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code float}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 4]}} is outside the readable range of
     *                                   this buffer.
     */
    default float readF32Be() {
        return Float.intBitsToFloat(readS32Be());
    }

    /**
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain a IEEE 32-bit binary float with little-endian byte order, and
     * converts them into a Java {@code float}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code float}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 4]}} is outside the readable range of
     *                                   this buffer.
     */
    default float readF32Le() {
        return Float.intBitsToFloat(readS32Le());
    }

    /**
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain a IEEE 32-bit binary float with the same endianess as the system
     * running this application, and converts them into a Java {@code float}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code float}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 4]}} is outside the readable range of
     *                                   this buffer.
     */
    default float readF32Ne() {
        return Float.intBitsToFloat(readS32Ne());
    }

    /**
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain a IEEE 64-bit binary float with big-endian byte order, and
     * converts them into a Java {@code double}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code double}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 8]}} is outside the readable range of
     *                                   this buffer.
     */
    default double readF64Be() {
        return Double.longBitsToDouble(readS64Be());
    }

    /**
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain a IEEE 64-bit binary float with little-endian byte order, and
     * converts them into a Java {@code double}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code double}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 8]}} is outside the readable range of
     *                                   this buffer.
     */
    default double readF64Le() {
        return Double.longBitsToDouble(readS64Le());
    }

    /**
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain a IEEE 64-bit binary float with the same endianess as the
     * system running this application, and converts them into a Java {@code
     * double}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code double}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 8]}} is outside the readable range of
     *                                   this buffer.
     */
    default double readF64Ne() {
        return Double.longBitsToDouble(readS64Ne());
    }

    /**
     * Gets and returns one byte from this buffer at its read offset.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code byte}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 1]}} is outside the readable range of
     *                                   this buffer.
     */
    default byte readS8() {
        final var buffer = new byte[1];
        read(buffer);
        return buffer[0];
    }

    /**
     * Gets two bytes from this buffer at its read offset, which are assumed to
     * contain a signed 16-bit integer with big-endian byte order, and converts
     * them into a Java {@code big-endian}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code short}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 2]}} is outside the readable range of
     *                                   this buffer.
     */
    default short readS16Be() {
        final var buffer = new byte[2];
        read(buffer);
        return BinaryMath.getS16BeAt(buffer, 0);
    }

    /**
     * Gets two bytes from this buffer at its read offset, which are assumed to
     * contain a signed 16-bit integer with little-endian byte order, and
     * converts them into a Java {@code little-endian}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code short}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 2]}} is outside the readable range of
     *                                   this buffer.
     */
    default short readS16Le() {
        final var buffer = new byte[2];
        read(buffer);
        return BinaryMath.getS16LeAt(buffer, 0);
    }

    /**
     * Gets two bytes from this buffer at its read offset, which are assumed to
     * contain a signed 16-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code short}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code short}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 2]}} is outside the readable range of
     *                                   this buffer.
     */
    default short readS16Ne() {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return readS16Be();
        }
        else {
            return readS16Le();
        }
    }

    /**
     * Gets three bytes from this buffer at its read offset, which are assumed
     * to contain a signed 24-bit integer with big-endian byte order, and
     * converts them into a Java {@code big-endian}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 3]}} is outside the readable range of
     *                                   this buffer.
     */
    default int readS24Be() {
        var value = readU24Be();
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    /**
     * Gets three bytes from this buffer at its read offset, which are assumed
     * to contain a signed 24-bit integer with little-endian byte order, and
     * converts them into a Java {@code little-endian}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 3]}} is outside the readable range of
     *                                   this buffer.
     */
    default int readS24Le() {
        var value = readU24Le();
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }


    /**
     * Gets three bytes from this buffer at its read offset, which are assumed
     * to contain a signed 24-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 3]}} is outside the readable range of
     *                                   this buffer.
     */
    default int readS24Ne() {
        var value = readU24Ne();
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    /**
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain a signed 32-bit integer with big-endian byte order, and converts
     * them into a Java {@code big-endian}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 4]}} is outside the readable range of
     *                                   this buffer.
     */
    default int readS32Be() {
        final var buffer = new byte[4];
        read(buffer);
        return BinaryMath.getS32BeAt(buffer, 0);
    }

    /**
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain a signed 32-bit integer with little-endian byte order, and
     * converts them into a Java {@code little-endian}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 4]}} is outside the readable range of
     *                                   this buffer.
     */
    default int readS32Le() {
        final var buffer = new byte[4];
        read(buffer);
        return BinaryMath.getS32LeAt(buffer, 0);
    }

    /**
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain a signed 32-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 4]}} is outside the readable range of
     *                                   this buffer.
     */
    default int readS32Ne() {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return readS32Be();
        }
        else {
            return readS32Le();
        }
    }

    /**
     * Gets six bytes from this buffer at its read offset, which are assumed to
     * contain a signed 48-bit integer with big-endian byte order, and converts
     * them into a Java {@code big-endian}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 6]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readS48Be() {
        var value = readU48Be();
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }

    /**
     * Gets six bytes from this buffer at its read offset, which are assumed to
     * contain a signed 48-bit integer with little-endian byte order, and
     * converts them into a Java {@code little-endian}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 6]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readS48Le() {
        var value = readU48Le();
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }


    /**
     * Gets six bytes from this buffer at its read offset, which are assumed to
     * contain a signed 48-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 6]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readS48Ne() {
        var value = readU48Ne();
        if ((value & 0x800000000000L) != 0) {
            value |= 0xffff000000000000L;
        }
        return value;
    }

    /**
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain a signed 64-bit integer with big-endian byte order, and
     * converts them into a Java {@code big-endian}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 8]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readS64Be() {
        final var buffer = new byte[8];
        read(buffer);
        return BinaryMath.getS64BeAt(buffer, 0);
    }


    /**
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain a signed 64-bit integer with little-endian byte order, and
     * converts them into a Java {@code little-endian}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 8]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readS64Le() {
        final var buffer = new byte[8];
        read(buffer);
        return BinaryMath.getS64LeAt(buffer, 0);
    }


    /**
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain a signed 64-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 8]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readS64Ne() {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return readS64Be();
        }
        else {
            return readS64Le();
        }
    }


    /**
     * Gets and returns one unsigned byte from this buffer at its read offset.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Unsigned byte as java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 1]}} is outside the readable range of
     *                                   this buffer.
     */
    default int readU8() {
        return Byte.toUnsignedInt(readS8());
    }

    /**
     * Gets two bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 16-bit integer with big-endian byte order, and
     * converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 16-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 2]}} is outside the readable range of
     *                                   this buffer.
     */
    default int readU16Be() {
        return Short.toUnsignedInt(readS16Be());
    }

    /**
     * Gets two bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 16-bit integer with little-endian byte order, and
     * converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 16-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 2]}} is outside the readable range of
     *                                   this buffer.
     */
    default int readU16Le() {
        return Short.toUnsignedInt(readS16Le());
    }

    /**
     * Gets two bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 16-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 16-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 2]}} is outside the readable range of
     *                                   this buffer.
     */
    default int readU16Ne() {
        return Short.toUnsignedInt(readS16Ne());
    }

    /**
     * Gets three bytes from this buffer at its read offset, which are assumed
     * to contain an unsigned 24-bit integer with big-endian byte order, and
     * converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 24-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 3]}} is outside the readable range of
     *                                   this buffer.
     */
    default int readU24Be() {
        final var buffer = new byte[3];
        read(buffer);
        return BinaryMath.getU24BeAt(buffer, 0);
    }

    /**
     * Gets three bytes from this buffer at its read offset, which are assumed
     * to contain an unsigned 24-bit integer with little-endian byte order, and
     * converts them into a Java {@code int}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 24-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 3]}} is outside the readable range of
     *                                   this buffer.
     */
    default int readU24Le() {
        final var buffer = new byte[3];
        read(buffer);
        return BinaryMath.getU24LeAt(buffer, 0);
    }

    /**
     * Gets three bytes from this buffer at its read offset, which are assumed
     * to contain an unsigned 24-bit integer with the same endianess as the
     * system running this application, and converts them into a Java {@code
     * int}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 24-bit integer as Java {@code int}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 3]}} is outside the readable range of
     *                                   this buffer.
     */
    default int readU24Ne() {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return readU24Be();
        }
        else {
            return readU24Le();
        }
    }

    /**
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 32-bit integer with big-endian byte order, and
     * converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 32-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 4]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readU32Be() {
        return Integer.toUnsignedLong(readS32Be());
    }

    /**
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 32-bit integer with little-endian byte order, and
     * converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 32-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 4]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readU32Le() {
        return Integer.toUnsignedLong(readS32Le());
    }

    /**
     * Gets four bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 32-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 32-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 4]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readU32Ne() {
        return Integer.toUnsignedLong(readS32Ne());
    }

    /**
     * Gets six bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 48-bit integer with big-endian byte order, and
     * converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 48-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 6]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readU48Be() {
        final var buffer = new byte[6];
        read(buffer);
        return BinaryMath.getU48BeAt(buffer, 0);
    }

    /**
     * Gets six bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 48-bit integer with little-endian byte order, and
     * converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 48-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 6]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readU48Le() {
        final var buffer = new byte[6];
        read(buffer);
        return BinaryMath.getU48LeAt(buffer, 0);
    }

    /**
     * Gets six bytes from this buffer at its read offset, which are assumed to
     * contain an unsigned 48-bit integer with the same endianess as the system
     * running this application, and converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     *
     * @return Unsigned 48-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 6]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readU48Ne() {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return readU48Be();
        }
        else {
            return readU48Le();
        }
    }

    /**
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain an unsigned 64-bit integer with big-endian byte order, and
     * converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     * <p>
     * This method is simply an alias for {@link #readS64Be()}, as there is no
     * way to represent unsigned 64-bit integers using a primitive type in Java
     * 11.
     *
     * @return Unsigned 64-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 8]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readU64Be() {
        return readS64Be();
    }

    /**
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain an unsigned 64-bit integer with little-endian byte order, and
     * converts them into a Java {@code long}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     * <p>
     * This method is simply an alias for {@link #readS64Le()}, as there is no
     * way to represent unsigned 64-bit integers using a primitive type in Java
     * 11.
     *
     * @return Unsigned 64-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 8]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readU64Le() {
        return readS64Le();
    }

    /**
     * Gets eight bytes from this buffer at its read offset, which are assumed
     * to contain an unsigned 64-bit integer with the same endianess as the
     * system running this application, and converts them into a Java {@code
     * long}.
     * <p>
     * The read offset of this buffer is updated by this operation.
     * <p>
     * This method is simply an alias for {@link #readS64Le()}, as there is no
     * way to represent unsigned 64-bit integers using a primitive type in Java
     * 11.
     *
     * @return Unsigned 64-bit integer as Java {@code long}.
     * @throws BufferIsClosed            If this buffer is closed.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + 8]}} is outside the readable range of
     *                                   this buffer.
     */
    default long readU64Ne() {
        return readS64Ne();
    }

    /**
     * Increases the read offset of this buffer with {@code length} bytes.
     *
     * @param length Number of bytes to skip reading from this buffer.
     * @throws BufferIsClosed            If this buffer is closed. Not
     *                                   guaranteed to be thrown by all
     *                                   implementations.
     * @throws IndexOutOfBoundsException If {@code {[readOffset(), readOffset()
     *                                   + length]}} is outside the readable
     *                                   range of this buffer.
     */
    void skip(int length);
}
