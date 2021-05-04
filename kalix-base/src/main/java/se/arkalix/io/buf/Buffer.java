package se.arkalix.io.buf;

import io.netty.buffer.Unpooled;
import se.arkalix.io.buf._internal.DefaultBufferReader;
import se.arkalix.io.buf._internal.DefaultBufferWriter;
import se.arkalix.io.buf._internal.EmptyBuffer;
import se.arkalix.io.buf._internal.NettyBuffer;

import java.nio.ByteBuffer;

public interface Buffer extends BufferReader, BufferWriter {
    static Buffer allocate(final int initialCapacity, final int maximumCapacity) {
        if (initialCapacity < 0 || initialCapacity > maximumCapacity) {
            throw new IndexOutOfBoundsException();
        }
        return new NettyBuffer(Unpooled.buffer(initialCapacity, maximumCapacity));
    }

    static Buffer allocateDirect(final int initialCapacity, final int maximumCapacity) {
        if (initialCapacity < 0 || initialCapacity > maximumCapacity) {
            throw new IndexOutOfBoundsException();
        }
        return new NettyBuffer(Unpooled.directBuffer(initialCapacity, maximumCapacity));
    }

    static Buffer empty() {
        return EmptyBuffer.instance();
    }

    static Buffer wrap(final ByteBuffer byteBuffer) {
        return new NettyBuffer(Unpooled.wrappedBuffer(byteBuffer));
    }

    static Buffer wrap(final byte[] byteArray) {
        return new NettyBuffer(Unpooled.wrappedBuffer(byteArray));
    }

    default BufferReader asReader() {
        return new DefaultBufferReader(this);
    }

    default BufferWriter asWriter() {
        return new DefaultBufferWriter(this);
    }

    void clear();

    default Buffer copy() {
        return copy(readOffset(), readableBytes());
    }

    Buffer copy(int offset, int length);

    Buffer dupe();

    void offsets(int readOffset, int writeOffset);

    @Override
    default int readEnd() {
        return writeOffset();
    }
}
