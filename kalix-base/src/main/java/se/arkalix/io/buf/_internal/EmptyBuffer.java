package se.arkalix.io.buf._internal;

import se.arkalix.io.buf.Buffer;
import se.arkalix.io.buf.BufferReader;
import se.arkalix.io.buf.BufferWriter;
import se.arkalix.util.annotation.Internal;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@Internal
public class EmptyBuffer implements Buffer {
    private static final EmptyBuffer instance = new EmptyBuffer();

    public static EmptyBuffer instance() {
        return instance;
    }

    private EmptyBuffer() {}

    @Override
    public void clear() {
        // Does nothing.
    }

    @Override
    public void offsets(final int readOffset, final int writeOffset) {
        if (readOffset != 0 || writeOffset != 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void close() {
        // Does nothing.
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void setAt(final int offset, final BufferReader source, final int sourceOffset, final int length) {
        if (offset != 0 || length != 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void setAt(final int offset, final ByteBuffer source) {
        if (offset != 0 || source.remaining() != 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int setAt(final int offset, final ReadableByteChannel source, final int maxLength) {
        if (offset != 0) {
            throw new IndexOutOfBoundsException();
        }
        return 0;
    }

    @Override
    public int writeOffset() {
        return 0;
    }

    @Override
    public void writeOffset(final int writeOffset) {
        if (writeOffset != 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int writeEnd() {
        return 0;
    }

    @Override
    public void write(final BufferReader source, final int sourceOffset, final int length) {
        if (length != 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void write(final ByteBuffer source) {
        if (source.remaining() != 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int write(final ReadableByteChannel source, final int maxLength) {
        return 0;
    }

    @Override
    public void getAt(final int offset, final BufferWriter destination, final int destinationOffset, final int length) {
        if (offset != 0 || length != 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void getAt(final int offset, final ByteBuffer destination) {
        if (offset != 0 || destination.remaining() != 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int getAt(final int offset, final WritableByteChannel destination, final int maxLength) {
        if (offset != 0) {
            throw new IndexOutOfBoundsException();
        }
        return 0;
    }

    @Override
    public int readOffset() {
        return 0;
    }

    @Override
    public void readOffset(final int readOffset) {
        if (readOffset != 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void read(final BufferWriter destination, final int destinationOffset, final int length) {
        if (length != 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void read(final ByteBuffer destination) {
        if (destination.remaining() != 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int read(final WritableByteChannel destination, final int maxLength) {
        return 0;
    }

    @Override
    public void skip(final int length) {
        if (length != 0) {
            throw new IndexOutOfBoundsException();
        }
    }
}
