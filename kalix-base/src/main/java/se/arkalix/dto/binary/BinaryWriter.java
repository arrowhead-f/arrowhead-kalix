package se.arkalix.dto.binary;

/**
 * Represents an object useful for <i>writing</i> binary data.
 * <p>
 * Apart from keeping track of whatever target is being written to, the writer
 * is also assumed to have an internal {@link #writeOffset() write offset} that
 * is incremented whenever bytes are written.
 */
public interface BinaryWriter {
    /**
     * Gets position in target at which the next written byte will be written
     * to.
     *
     * @return Current write offset.
     */
    int writeOffset();

    /**
     * Sets new write offset.
     *
     * @param offset New write offset.
     * @throws IndexOutOfBoundsException If given {@code offset} is less than 0
     *                                   or larger than the number of {@link
     *                                   #writableBytes() writable bytes}.
     */
    void writeOffset(int offset);

    /**
     * Gets the number of bytes of space currently available in the target
     * written to.
     * <p>
     * The underlying target might be able to allocate more memory as it fills
     * up. The number returned by this method may or may not represent a final
     * amount of remaning space.
     *
     * @return Remaining number of writable bytes.
     */
    int writableBytes();

    /**
     * Writes byte to target at the current {@link #writeOffset() write offset}
     * and then increments that offset.
     *
     * @param b Byte to write.
     * @throws IndexOutOfBoundsException If there is no space left to write to.
     */
    void write(byte b);

    /**
     * Writes {@code bytes} to target at the current {@link #writeOffset()
     * write offset}.
     *
     * @param bytes Bytes to write.
     * @throws IndexOutOfBoundsException If less than {@code bytes.length}
     *                                   bytes of space remains in target.
     */
    void write(byte[] bytes);
}
