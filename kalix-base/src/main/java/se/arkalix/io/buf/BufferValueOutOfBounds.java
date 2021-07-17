package se.arkalix.io.buf;

/**
 * Signifies the attempt to write a value that cannot be represented under the
 * conditions in which the write was issued.
 * <p>
 * This exception could, for example, be thrown if attempting to write {@code
 * -5} as an unsigned 8-bit value to a {@link Buffer}, as {@code -5} is outside
 * the range of representable 8-bit unsigned values.
 */
public class BufferValueOutOfBounds extends BufferException {
    private final long min;
    private final long max;
    private final long value;

    /**
     * Creates new exception.
     *
     * @param min   Smallest acceptable value.
     * @param max   Largest acceptable value.
     * @param value Offending value.
     */
    public BufferValueOutOfBounds(final long min, final long max, final long value) {
        this.min = min;
        this.max = max;
        this.value = value;
    }

    /**
     * Smallest value acceptable to write.
     *
     * @return Smallest acceptable value.
     */
    public long min() {
        return min;
    }

    /**
     * Largest value acceptable to write.
     *
     * @return Largest acceptable value.
     */
    public long max() {
        return max;
    }

    /**
     * Actual value written.
     *
     * @return Actual value written.
     */
    public long value() {
        return value;
    }

    @Override
    public String getMessage() {
        return "buffer value " + value() + " not in [" + min() + ", " + max() + "]";
    }
}
