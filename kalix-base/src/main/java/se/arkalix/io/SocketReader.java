package se.arkalix.io;

import se.arkalix.io.buf.BufferWriter;

public interface SocketReader extends AutoCloseable {
    @Override
    void close();

    boolean isClosed();

    int read(final BufferWriter destination);
}
