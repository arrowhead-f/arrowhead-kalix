package se.arkalix.io;

import se.arkalix.concurrent.Future;
import se.arkalix.io.buffer.BufferReader;

public interface Socket<S extends Socket<?>> extends SocketOptions<S> {
    void close();

    Future<?> send(BufferReader buffer);

    default Future<?> sendAndClose(final BufferReader buffer) {
        return send(buffer)
            .and(this::close);
    }
}
