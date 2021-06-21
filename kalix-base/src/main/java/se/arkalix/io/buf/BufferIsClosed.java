package se.arkalix.io.buf;

/**
 * Signifies that a {@link Buffer} operation failed to complete due to the
 * {@link Buffer} in question being {@link Buffer#close() closed}.
 */
public class BufferIsClosed extends BufferException {
    @Override
    public String getMessage() {
        return "buffer is closed";
    }
}
