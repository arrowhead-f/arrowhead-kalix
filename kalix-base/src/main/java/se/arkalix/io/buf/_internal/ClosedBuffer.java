package se.arkalix.io.buf._internal;

import se.arkalix.io.buf.Buffer;
import se.arkalix.io.buf.BufferIsClosed;
import se.arkalix.io.buf.BufferReader;
import se.arkalix.io.buf.BufferWriter;
import se.arkalix.util.annotation.Internal;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@Internal
public class ClosedBuffer implements Buffer {
    private static final ClosedBuffer instance = new ClosedBuffer();

    public static ClosedBuffer instance() {
        return instance;
    }

    private ClosedBuffer() {}

    @Override
    public void clear() {
        throw new BufferIsClosed();
    }

    @Override
    public void offsets(final int readOffset, final int writeOffset) {
        throw new BufferIsClosed();
    }

    @Override
    public int readOffset() {
        throw new BufferIsClosed();
    }

    @Override
    public void readOffset(final int readOffset) {
        throw new BufferIsClosed();
    }

    @Override
    public void getAt(final int offset, final BufferWriter destination, final int destinationOffset, final int length) {
        throw new BufferIsClosed();
    }

    @Override
    public void getAt(final int offset, final ByteBuffer destination) {
        throw new BufferIsClosed();
    }

    @Override
    public int getAt(final int offset, final WritableByteChannel destination, final int maxLength) {
        throw new BufferIsClosed();
    }

    @Override
    public void read(final BufferWriter destination, final int destinationOffset, final int length) {
        throw new BufferIsClosed();
    }

    @Override
    public void read(final ByteBuffer destination) {
        throw new BufferIsClosed();
    }

    @Override
    public int read(final WritableByteChannel destination, final int maxLength) {
        throw new BufferIsClosed();
    }

    @Override
    public void skip(final int length) {
        throw new BufferIsClosed();
    }

    @Override
    public int writeEnd() {
        throw new BufferIsClosed();
    }

    @Override
    public int writeOffset() {
        throw new BufferIsClosed();
    }

    @Override
    public void writeOffset(final int writeOffset) {
        throw new BufferIsClosed();
    }

    @Override
    public void setAt(final int offset, final BufferReader source, final int sourceOffset, final int length) {
        throw new BufferIsClosed();
    }

    @Override
    public void setAt(final int offset, final ByteBuffer source) {
        throw new BufferIsClosed();
    }

    @Override
    public int setAt(final int offset, final ReadableByteChannel source, final int maxLength) {
        throw new BufferIsClosed();
    }

    @Override
    public void write(final BufferReader source, final int sourceOffset, final int length) {
        throw new BufferIsClosed();
    }

    @Override
    public void write(final ByteBuffer source) {
        throw new BufferIsClosed();
    }

    @Override
    public int write(final ReadableByteChannel source, final int maxLength) {
        throw new BufferIsClosed();
    }

    @Override
    public boolean isClosed() {
        return true;
    }

    @Override
    public void close() {
        // Does nothing.
    }
}
