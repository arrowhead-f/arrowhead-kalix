package se.arkalix.io.buf;

public class BufferValueOutOfBounds extends BufferException {
    private final long min;
    private final long max;
    private final long value;

    public BufferValueOutOfBounds(final long min, final long max, final long value) {
        this.min = min;
        this.max = max;
        this.value = value;
    }

    public long min() {
        return min;
    }

    public long max() {
        return max;
    }

    public long value() {
        return value;
    }

    @Override
    public String getMessage() {
        return "buffer value " + value() + " not in [" + min() + ", " + max() + "]";
    }
}
