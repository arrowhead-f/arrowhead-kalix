package se.arkalix.io.buf;

import io.netty.buffer.Unpooled;
import se.arkalix.io.buf._internal.DefaultBufferReader;
import se.arkalix.io.buf._internal.DefaultBufferWriter;
import se.arkalix.io.buf._internal.EmptyBuffer;
import se.arkalix.io.buf._internal.NettyBuffer;

import java.nio.ByteBuffer;

/**
 * A collection of memory that can be read from or written to.
 */
public interface Buffer extends BufferReader, BufferWriter {
    /**
     * Allocates a new buffer with given initial capacity and maximum capacity.
     * <p>
     * If the initial capacity is exhausted by writing memory to the buffer, it
     * will be reallocated until it reaches the maximum capacity. The buffer
     * will reside in memory managed by the JVM.
     *
     * @param initialCapacity Desired initial capacity, in bytes.
     * @param maximumCapacity Desired maximum capacity, in bytes.
     * @return Allocated buffer.
     */
    static Buffer allocate(final int initialCapacity, final int maximumCapacity) {
        if (initialCapacity < 0 || initialCapacity > maximumCapacity) {
            throw new IndexOutOfBoundsException();
        }
        return new NettyBuffer(Unpooled.buffer(initialCapacity, maximumCapacity));
    }

    /**
     * Allocates a new direct buffer with given initial and maximum capacities.
     * <p>
     * If the initial capacity is exhausted by writing memory to the buffer, it
     * will be reallocated until it reaches the maximum capacity. The buffer
     * will reside in memory <i>not</i> managed by the JVM. Direct memory can
     * be faster for operations that require interaction with the operating
     * system, such as writing to a socket or reading from a file.
     *
     * @param initialCapacity Desired initial capacity, in bytes.
     * @param maximumCapacity Desired maximum capacity, in bytes.
     * @return Allocated direct memory buffer.
     */
    static Buffer allocateDirect(final int initialCapacity, final int maximumCapacity) {
        if (initialCapacity < 0 || initialCapacity > maximumCapacity) {
            throw new IndexOutOfBoundsException();
        }
        return new NettyBuffer(Unpooled.directBuffer(initialCapacity, maximumCapacity));
    }

    /**
     * Gets reference to singleton buffer that always reports being empty.
     *
     * @return Reference to empty buffer.
     */
    static Buffer empty() {
        return EmptyBuffer.instance();
    }

    /**
     * Wraps given {@link ByteBuffer} into a {@link Buffer}.
     *
     * @param byteBuffer Buffer to wrap.
     * @return Wrapped buffer.
     */
    static Buffer wrap(final ByteBuffer byteBuffer) {
        return new NettyBuffer(Unpooled.wrappedBuffer(byteBuffer));
    }

    /**
     * Wraps given byte array into a {@link Buffer}.
     * <p>
     * If you want to write to the wrapped byte array through the returned
     * buffer, make sure to call {@link #clear()} before you write.
     *
     * @param byteArray Byte array to wrap.
     * @return Wrapped byte array.
     */
    static Buffer wrap(final byte[] byteArray) {
        return new NettyBuffer(Unpooled.wrappedBuffer(byteArray));
    }

    /**
     * Wraps this buffer in an object that only allows for it to be read.
     * <p>
     * Note that while the returned buffer can only be read from, it accesses
     * the same memory as this buffer, which means that you can still write to
     * that memory by using this buffer.
     *
     * @return Read-only buffer.
     */
    default BufferReader asReader() {
        return new DefaultBufferReader(this);
    }

    /**
     * Wraps this buffer in an object that only allows for it to be written to.
     * <p>
     * Note that while the returned buffer can only be written to, it updates
     * the same memory as this buffer, which means that you can still read from
     * that memory by using this buffer.
     *
     * @return Write-only buffer.
     */
    default BufferWriter asWriter() {
        return new DefaultBufferWriter(this);
    }

    /**
     * Clears the contents of this buffer.
     * <p>
     * This method is only guaranteed to reset the internal read and write
     * offsets. It will likely not do anything to the actual buffer memory.
     */
    void clear();

    /**
     * Creates a new buffer containing a copy of the contents of this buffer.
     * <p>
     * Writing to the returned buffer does not affect this buffer, and vice
     * versa. The copy will only contain the bytes of this buffer that can be
     * read when the method was called. The internal read offset is not
     * affected by this method.
     *
     * @return Copy of this buffer.
     */
    default Buffer copy() {
        return copy(readOffset(), readableBytes());
    }

    /**
     * Creates a new buffer containing a copy of the specified byte range.
     * <p>
     * Writing to the returned buffer does not affect this buffer, and vice
     * versa. The internal read offset is not affected by this method.
     *
     * @param offset Position in this buffer from which copying is to start.
     * @param length Number of bytes from {@code offset} to copy.
     * @return Copy of this buffer.
     */
    Buffer copy(int offset, int length);

    /**
     * Creates a new buffer being a dupe of this buffer.
     * <p>
     * The dupe has its own internal read and write offsets, but shares memory
     * with this buffer.
     *
     * @return New dupe buffer.
     */
    Buffer dupe();

    /**
     * Updates the read and write offsets of this buffer atomically.
     * <p>
     * The read offset must be smaller than or equal to the write offset.
     *
     * @param readOffset  Desired read offset.
     * @param writeOffset Desired write offset.
     * @throws IndexOutOfBoundsException If the read offset is smaller than
     *                                   zero or larger than writeOffset.
     */
    void offsets(int readOffset, int writeOffset);

    @Override
    default int readEnd() {
        return writeOffset();
    }
}
