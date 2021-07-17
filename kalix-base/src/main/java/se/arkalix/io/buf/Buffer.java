package se.arkalix.io.buf;

import se.arkalix.io.buf._internal.DefaultBufferReader;
import se.arkalix.io.buf._internal.DefaultBufferWriter;
import se.arkalix.io.buf._internal.EmptyBuffer;
import se.arkalix.io.buf._internal.NioBuffer;

import java.nio.ByteBuffer;

/**
 * An owned collection of memory that can be read from and written to.
 * <p>
 * The memory contains as if being a contiguous region of bytes, where each byte
 * has its own <i>offset</i>, or position, relative to the beginning of the
 * region. Four pointers are maintained into the memory of the buffer. Those
 * are:
 * <ol>
 *     <li>{@link #readOffset()}: The offset of the first byte in the region of
 *     bytes that can be read from.
 *     <li>{@link #readEnd()}: The offset of the first byte outside the region
 *     of bytes that can be read from.
 *     <li>{@link #writeOffset()}: The offset of the first byte in the region
 *     of bytes that can be written to. A given buffer implementation <i>may</i>
 *     treat the {@link #readEnd()} pointer as an alias for this pointer, which
 *     would mean that they would always be equal.
 *     <li>{@link #writeEnd()}: The offset of the first byte outside the region
 *     of bytes that can be written to.
 * </ol>
 * Five kinds of read/write methods are provided for acquiring or
 * mutating those bytes, prefixed as follows:
 * <ul>
 *     <li><b>get</b>: Copies memory <i>from</i> this buffer, beginning at an
 *     explicitly specified offset. The <i>read offset</i> of this buffer is
 *     <i>not changed</i>.
 *     <li><b>peek</b>: Copies memory <i>from</i> this buffer, beginning at its
 *     current <i>read offset</i>. The read offset is <i>not changed</i>.
 *     <li><b>read</b>: Copies memory <i>from</i> this buffer, beginning at its
 *     current <i>read offset</i>. The read offset is increased to make it
 *     point to the first byte right after the read region.
 *     <li><b>set</b>: Copies memory <i>to</i> this buffer, beginning at an
 *     explicitly specified offset. The write offset of this buffer is <i>not
 *     changed</i>.
 *     <li><b>write</b>: Copies memory <i>to</i> this buffer, beginning at its
 *     current write offset. The write offset is increased to make it point to
 *     the first byte right after the memory region written to.
 * </ul>
 * When no longer in use, every allocated buffer <b>must</b> be {@link #close()
 * closed} exactly once. It is not enough to close any buffers derived from
 * this, for example via {@link #asWriter()}. Every buffer <b>must</b> be
 * closed at some point, or memory may be leaked. It should always be safe to
 * close a buffer more than once.
 * <p>
 * This interface is modeled after the Java NIO {@link ByteBuffer}, as well as
 * the {@code ByteBuf} of the Netty library. Its purpose is to make room for
 * automatic buffer recycling by being {@link #close() closeable}, as well as
 * to make buffer handling less prone to mistakes than its NIO counterpart,
 * primarily by relying on the same kinds of strategies as the Netty {@code
 * ByteBuf} class. It can, for example, support automatic memory reallocation
 * as capacity runs out, has separate read and write offsets, and requires that
 * endianess is decided upon when choosing what read or write method to invoke
 * rather than having an endianess property that can be changed at any time.
 *
 * @see BufferReader
 * @see BufferWriter
 */
public interface Buffer extends BufferReader, BufferWriter {
    /**
     * Allocates a new buffer with given initial capacity and maximum capacity.
     * <p>
     * If the initial capacity is exhausted by writing memory to the buffer, it
     * will be reallocated until it reaches the maximum capacity. The buffer
     * will reside in memory managed by the JVM.
     * <p>
     * The returned buffer <b>must</b> be {@link #close() closed} once no longer
     * in use.
     *
     * @param initialCapacity Desired initial capacity, in bytes.
     * @param maximumCapacity Desired maximum capacity, in bytes.
     * @return Allocated buffer.
     */
    static Buffer allocate(final int initialCapacity, final int maximumCapacity) {
        if (initialCapacity < 0 || initialCapacity > maximumCapacity) {
            throw new IndexOutOfBoundsException();
        }
        return new NioBuffer(ByteBuffer.allocate(initialCapacity), maximumCapacity);
    }

    /**
     * Allocates a new direct buffer with given initial and maximum capacities.
     * <p>
     * If the initial capacity is exhausted by writing memory to the buffer, it
     * will be reallocated until it reaches the maximum capacity. The buffer
     * will reside in memory <i>not</i> managed by the JVM. Direct memory can be
     * faster for operations that require interaction with the operating system,
     * such as writing to a socket or reading from a file.
     * <p>
     * The returned buffer <b>must</b> be {@link #close() closed} once no longer
     * in use.
     *
     * @param initialCapacity Desired initial capacity, in bytes.
     * @param maximumCapacity Desired maximum capacity, in bytes.
     * @return Allocated direct memory buffer.
     */
    static Buffer allocateDirect(final int initialCapacity, final int maximumCapacity) {
        if (initialCapacity < 0 || initialCapacity > maximumCapacity) {
            throw new IndexOutOfBoundsException();
        }
        return new NioBuffer(ByteBuffer.allocateDirect(initialCapacity), maximumCapacity);
    }

    /**
     * Gets reference to singleton buffer that always remains empty.
     *
     * @return Reference to empty buffer.
     */
    static Buffer empty() {
        return EmptyBuffer.instance();
    }

    /**
     * Wraps given {@link ByteBuffer} into a {@link Buffer}.
     * <p>
     * The returned buffer <b>should</b> be {@link #close() closed} once no
     * longer in use.
     *
     * @param byteBuffer Buffer to wrap.
     * @return Wrapped buffer.
     * @throws NullPointerException If {@code byteBuffer} is {@code null}.
     */
    static Buffer wrap(final ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            throw new NullPointerException("byteBuffer");
        }
        return new NioBuffer(byteBuffer.duplicate(), byteBuffer.capacity());
    }

    /**
     * Wraps given byte array into a {@link Buffer}.
     * <p>
     * If you want to write to the wrapped byte array through the returned
     * buffer, make sure to call {@link #clear()} before you write.
     * <p>
     * The returned buffer <b>should</b> be {@link #close() closed} once no
     * longer in use.
     *
     * @param byteArray Byte array to wrap.
     * @return Wrapped byte array.
     * @throws NullPointerException If {@code byteArray} is {@code null}.
     */
    static Buffer wrap(final byte[] byteArray) {
        return wrap(byteArray, 0, byteArray.length);
    }

    /**
     * Wraps identified range of given byte array into a {@link Buffer}.
     * <p>
     * If you want to write to the wrapped byte array through the returned
     * buffer, make sure to call {@link #clear()} before you write.
     * <p>
     * The returned buffer <b>should</b> be {@link #close() closed} once no
     * longer in use.
     *
     * @param byteArray Byte array to wrap.
     * @param offset    Offset from beginning of {@code byteArray}, in bytes.
     * @param length    Number of bytes, beginning at {@code offset}, to include
     *                  in the returned {@link Buffer}.
     * @return Wrapped byte array.
     * @throws NullPointerException If {@code byteArray} is {@code null}.
     */
    static Buffer wrap(final byte[] byteArray, final int offset, final int length) {
        if (byteArray == null) {
            throw new NullPointerException("byteArray");
        }
        return new NioBuffer(ByteBuffer.wrap(byteArray, offset, length), length);
    }

    /**
     * Wraps this buffer in an object that only allows for it to be read.
     * <p>
     * Note that while the returned buffer can only be read from, it accesses
     * the same memory as this buffer, which means that you can still write to
     * that memory by using this buffer. Closing this buffer causes the returned
     * buffer to be closed by default. This behavior may, however, be changed by
     * classes implementing this interface.
     * <p>
     * The returned buffer <b>should</b> be {@link #close() closed} once no
     * longer in use. The reason for this is that <i>if</i> this buffer
     * implementation uses some sort of reference counting schema, its memory
     * may end up being leaked if this reader is not closed.
     *
     * @return Read-only buffer.
     * @throws BufferIsClosed If this buffer is closed. Not guaranteed to be
     *                        thrown by all implementations.
     */
    default BufferReader asReader() {
        return new DefaultBufferReader(this);
    }

    /**
     * Wraps this buffer in an object that only allows for it to be written to.
     * <p>
     * Note that while the returned buffer can only be written to, it updates
     * the same memory as this buffer, which means that you can still read from
     * that memory by using this buffer. Closing this buffer causes the returned
     * buffer to be closed by default. This behavior may, however, be changed by
     * classes implementing this interface.
     * <p>
     * The returned buffer <b>should</b> be {@link #close() closed} once no
     * longer in use. The reason for this is that <i>if</i> this buffer
     * implementation uses some sort of reference counting schema, its memory
     * may end up being leaked if this reader is not closed.
     *
     * @return Write-only buffer.
     * @throws BufferIsClosed If this buffer is closed. Not guaranteed to be
     *                        thrown by all implementations.
     */
    default BufferWriter asWriter() {
        return new DefaultBufferWriter(this);
    }

    /**
     * Clears the contents of this buffer.
     * <p>
     * This method is only guaranteed to reset the internal read and write
     * offsets. It will likely not do anything to the actual buffer memory.
     *
     * @throws BufferIsClosed If this buffer is closed. Not guaranteed to be
     *                        thrown by all implementations.
     */
    void clear();

    /**
     * Updates the read and write offsets of this buffer atomically.
     * <p>
     * The read offset must be smaller than or equal to the write offset.
     * <p>
     * This method is preferred over calling {@link #readOffset(int)} and {@link
     * #writeOffset(int)} separately right after each other. Using this method
     * means that fewer bounds checks have to be performed, as well as there
     * being no risk of problems arising due to the two methods being called in
     * a problematic order. The bounds checks of those two methods could fail
     * even though same values would have succeeded if provided to this method.
     *
     * @param readOffset  Desired read offset.
     * @param writeOffset Desired write offset.
     * @throws BufferIsClosed            If this buffer is closed. Not
     *                                   guaranteed to be thrown by all
     *                                   implementations.
     * @throws IndexOutOfBoundsException If the {@code readOffset} is smaller
     *                                   than zero or larger than {@code
     *                                   writeOffset}.
     * @see #readOffset(int)
     * @see #writeOffset(int)
     */
    void offsets(int readOffset, int writeOffset);

    @Override
    default int readEnd() {
        return writeOffset();
    }
}
