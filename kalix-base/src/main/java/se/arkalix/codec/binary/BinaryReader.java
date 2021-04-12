package se.arkalix.codec.binary;

/**
 * Represents an object useful for <i>reading</i> binary data.
 * <p>
 * Apart from keeping track of whatever source of bytes is being read, the
 * reader is also assumed to have an internal {@link #readOffset() read offset}
 * that is incremented whenever bytes are read.
 */
@SuppressWarnings("unused")
public interface BinaryReader {
    /**
     * @return Index of next byte to read.
     */
    int readOffset();

    /**
     * @param offset New read offset.
     * @throws IndexOutOfBoundsException If given {@code offset} is less than 0
     *                                   or larger than the number of {@link
     *                                   #readableBytes() readable bytes}.
     */
    void readOffset(int offset);

    /**
     * @return Number of bytes currently left to read.
     */
    int readableBytes();

    /**
     * Gets byte at the given {@code offset} without incrementing the internal
     * {@link #readOffset() read offset}.
     *
     * @param offset Position of byte to read.
     * @return Read byte.
     * @throws IndexOutOfBoundsException If given {@code offset} is less than 0
     *                                   or larger than the number of {@link
     *                                   #readableBytes() readable bytes}.
     */
    byte getByte(int offset);

    /**
     * Gets bytes at the given {@code offset} without incrementing the internal
     * {@link #readOffset() read offset}. The number of acquired bytes will be
     * the same as the length of {@code target}.
     *
     * @param offset Position of byte to read.
     * @param target Receiver of read bytes.
     * @throws IndexOutOfBoundsException If given {@code offset} is less than 0
     *                                   or larger than the number of {@link
     *                                   #readableBytes() readable bytes} minus
     *                                   the length of {@code target}.
     */
    void getBytes(int offset, byte[] target);

    /**
     * Gets bytes at the given {@code offset} without incrementing the internal
     * {@link #readOffset() read offset}. The number of acquired bytes will be
     * the same as the length of {@code target}.
     *
     * @param offset       Position of byte to read.
     * @param target       Receiver of read bytes.
     * @param targetOffset Offset from beginning of {@code target} at which the
     *                     received bytes will be written.
     * @param length       The number of bytes to read into {@code target}.
     * @throws IndexOutOfBoundsException If {@code  offset}, {@code
     *                                   targetOffset} or {@code length} are
     *                                   negative, out of bounds, or there are
     *                                   less than {@code length} bytes left to
     *                                   read.
     */
    void getBytes(final int offset, final byte[] target, final int targetOffset, final int length);

    /**
     * Gets byte at the internal {@link #readOffset() read offset}, without
     * incrementing the offset.
     *
     * @return Byte at the internal {@link #readOffset() read offset}.
     * @throws IndexOutOfBoundsException If there is no byte left to peek.
     */
    byte peekByte();

    /**
     * Gets byte at the internal {@link #readOffset() read offset} and
     * increments that offset.
     *
     * @return Byte at the internal {@link #readOffset() read offset}.
     * @throws IndexOutOfBoundsException If there is no byte left to read.
     */
    byte readByte();

    /**
     * Gets byte at the internal {@link #readOffset() read offset} and
     * increments that offset, unless there is nothing more to read, in which
     * case {@code 0} is returned.
     *
     * @return Byte at the internal {@link #readOffset() read offset} or
     * {@code 0}.
     */
    default byte readByteOrZero() {
        return readableBytes() > 0
            ? readByte()
            : 0;
    }

    /**
     * Reads bytes into {@code target}. The number of read bytes will be the
     * same as the length of {@code target}.
     *
     * @param target Receiver of read bytes.
     * @throws IndexOutOfBoundsException If less than {@code target.length}
     *                                   bytes are available for reading.
     */
    default void readBytes(byte[] target) {
        readBytes(target, 0, target.length);
    }

    /**
     * Reads {@code length} bytes into {@code target}, beginning at
     * {@code targetOffset}.
     *
     * @param target       Receiver of read bytes.
     * @param targetOffset Position in {@code target} at which to start adding
     *                     read bytes.
     * @param length       Number of bytes to read into {@code target}.
     * @throws IndexOutOfBoundsException If {@code targetOffset} or {@code
     *                                   length} are negative, out of bounds,
     *                                   or there are less than {@code length}
     *                                   bytes left to read.
     */
    void readBytes(byte[] target, int targetOffset, int length);

    /**
     * Increments the internal {@link #readOffset() read offset} by 1 without
     * getting the byte at the current offset.
     *
     * @throws IndexOutOfBoundsException If there is no byte to skip.
     */
    default void skipByte() {
        skipBytes(1);
    }

    /**
     * Increments the internal {@link #readOffset() read offset} by {@code n}.
     *
     * @param n Number of bytes to skip.
     * @throws IndexOutOfBoundsException If there are less than {@code n} bytes
     *                                   left to skip.
     */
    void skipBytes(int n);
}
