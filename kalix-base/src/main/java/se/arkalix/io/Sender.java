package se.arkalix.io;

import se.arkalix.io.buf.BufferReader;
import se.arkalix.util.concurrent.Future;

public interface Sender<D> extends Future<D> {
    Future<?> send(BufferReader buffer);
}
