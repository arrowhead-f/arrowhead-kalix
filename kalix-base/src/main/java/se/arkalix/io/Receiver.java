package se.arkalix.io;

import se.arkalix.io.buf.BufferReader;
import se.arkalix.util.concurrent.Future;

@FunctionalInterface
public interface Receiver<S extends Sender<?>> {
    Future<?> receive(S sender, BufferReader buffer) throws Throwable;
}
