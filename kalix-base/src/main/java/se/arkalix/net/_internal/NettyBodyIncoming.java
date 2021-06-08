package se.arkalix.net._internal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import se.arkalix.io.buf.BufferReader;
import se.arkalix.net.BodyIncoming;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import java.nio.file.Path;
import java.util.Objects;

@Internal
public class NettyBodyIncoming implements BodyIncoming, NettyBodyReceiver {
    private final ByteBufAllocator allocator;

    private NettyBodyReceiver receiver = null;
    private boolean isDone = true;

    public NettyBodyIncoming(final ByteBufAllocator allocator) {
        this.allocator = Objects.requireNonNull(allocator, "allocator");
    }

    @Override
    public Future<BufferReader> buffer() {
        if (receiver != null) {
            throw new IllegalStateException("Message body already consumed");
        }
        final var receiver = new NettyBodyReceiverBuffered(allocator);
        this.receiver = receiver;
        isDone = false;
        return receiver;
    }

    @Override
    public Future<?> writeTo(final Path path, final boolean append) {
        if (receiver != null) {
            throw new IllegalStateException("Message body already consumed");
        }
        final var receiver = new NettyBodyReceiverFileWriter(path, append);
        this.receiver = receiver;
        isDone = false;
        return receiver;
    }

    public boolean isDone() {
        return isDone || isCancelled();
    }

    @Override
    public boolean isCancelled() {
        return receiver == null || receiver.isCancelled();
    }

    @Override
    public void abort(final Throwable cause) {
        isDone = true;
        receiver.abort(cause);
    }

    @Override
    public void write(final ByteBuf byteBuf) {
        receiver.write(byteBuf);
    }

    @Override
    public void close() {
        isDone = true;
        receiver.close();
    }
}
