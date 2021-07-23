package se.arkalix.io;

import se.arkalix.io.buffer.BufferReader;
import se.arkalix.util.concurrent.Future;

public interface Socket<S extends Socket<?>> extends SocketOptions<S> {
    void close();

    Future<?> send(BufferReader buffer);

    default Future<?> sendAndClose(final BufferReader buffer) {
        return send(buffer)
            .then(__ -> close());
    }
}
