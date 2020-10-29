package se.arkalix.net._internal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import se.arkalix.encoding.binary.BinaryReader;
import se.arkalix.net.BodyIncoming;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import java.nio.file.Path;
import java.util.Objects;

@Internal
public class NettyBodyIncoming implements BodyIncoming, NettyBodyReceiver {
    private final ByteBufAllocator allocator;

    private NettyBodyReceiver receiver;
    private boolean isRequested = false;
    private boolean isReceived = false;

    public NettyBodyIncoming(final ByteBufAllocator allocator) {
        this.allocator = Objects.requireNonNull(allocator, "allocator");
    }

    @Override
    public Future<BinaryReader> buffer() {
        if (receiver != null) {
            throw new IllegalStateException("Message body already consumed");
        }
        final var receiver = new NettyBodyReceiverBuffered(allocator);
        this.receiver = receiver;
        isRequested = true;
        return receiver;
    }

    @Override
    public Future<?> writeTo(final Path path, final boolean append) {
        if (receiver != null) {
            throw new IllegalStateException("Message body already consumed");
        }
        final var receiver = new NettyBodyReceiverFileWriter(path, append);
        this.receiver = receiver;
        isRequested = true;
        return receiver;
    }

    public boolean isRequested() {
        return isRequested;
    }

    public boolean isReceived() {
        return isReceived;
    }

    @Override
    public boolean isCancelled() {
        return receiver.isCancelled();
    }

    @Override
    public void abort(final Throwable cause) {
        isReceived = true;
        receiver.abort(cause);
    }

    @Override
    public void write(final ByteBuf byteBuf) {
        receiver.write(byteBuf);
    }

    @Override
    public void close() {
        isReceived = true;
        receiver.close();
    }
}
