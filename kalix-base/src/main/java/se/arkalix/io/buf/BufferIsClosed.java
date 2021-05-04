package se.arkalix.io.buf;

public class BufferIsClosed extends BufferException {
    @Override
    public String getMessage() {
        return "buffer is closed";
    }
}
