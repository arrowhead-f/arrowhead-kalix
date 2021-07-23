package se.arkalix.io.tcp._internal;

import se.arkalix.concurrent.SynchronizedPromise;
import se.arkalix.io.buffer.BufferReader;

import java.nio.channels.WritableByteChannel;
import java.util.Objects;

public class PromiseToSend extends SynchronizedPromise<Void> {
    private final BufferReader buffer;

    public PromiseToSend(final BufferReader buffer) {
        this.buffer = Objects.requireNonNull(buffer, "buffer");
    }

    public int send(final WritableByteChannel destination) {
        final int numberOfSentBytes = buffer.read(destination);
        if (buffer.readableBytes() == 0) {
            fulfill(null);
        }
        return numberOfSentBytes;
    }
}
