package se.arkalix.io.buf._internal;

import se.arkalix.io.buf.Buffer;
import se.arkalix.io.buf.BufferReader;
import se.arkalix.io.buf.BufferWriter;
import se.arkalix.util.annotation.Internal;

import java.nio.ByteBuffer;

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
    public void setAt(final int offset, final byte[] source, final int sourceOffset, final int length) {
        if (offset != 0 || length != 0) {
            throw new IndexOutOfBoundsException();
        }
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
    public void setS8At(final int offset, final byte value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS16BeAt(final int offset, final short value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS16NeAt(final int offset, final short value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS16LeAt(final int offset, final short value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS24BeAt(final int offset, final int value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS24NeAt(final int offset, final int value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS24LeAt(final int offset, final int value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS32BeAt(final int offset, final int value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS32NeAt(final int offset, final int value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS32LeAt(final int offset, final int value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS48BeAt(final int offset, final long value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS48NeAt(final int offset, final long value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS48LeAt(final int offset, final long value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS64BeAt(final int offset, final long value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS64NeAt(final int offset, final long value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setS64LeAt(final int offset, final long value) {
        throw new IndexOutOfBoundsException();
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
    public void write(final byte[] source, final int sourceOffset, final int length) {
        if (length != 0) {
            throw new IndexOutOfBoundsException();
        }
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
    public void writeS8(final byte value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS16Be(final short value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS16Le(final short value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS16Ne(final short value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS24Be(final int value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS24Le(final int value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS24Ne(final int value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS32Be(final int value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS32Le(final int value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS32Ne(final int value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS48Be(final long value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS48Le(final long value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS48Ne(final long value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS64Be(final long value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS64Le(final long value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeS64Ne(final long value) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void getAt(final int offset, final byte[] destination, final int destinationOffset, final int length) {
        if (offset != 0 || length != 0) {
            throw new IndexOutOfBoundsException();
        }
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
    public byte getS8At(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public short getS16BeAt(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public short getS16LeAt(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public short getS16NeAt(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getS32BeAt(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getS32LeAt(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getS32NeAt(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long getS64BeAt(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long getS64LeAt(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long getS64NeAt(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getU24BeAt(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getU24LeAt(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getU24NeAt(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long getU48BeAt(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long getU48LeAt(final int offset) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long getU48NeAt(final int offset) {
        throw new IndexOutOfBoundsException();
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
    public void read(final byte[] destination, final int destinationOffset, final int length) {
        if (length != 0) {
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
    public byte readS8() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public short readS16Be() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public short readS16Le() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public short readS16Ne() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int readS32Be() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int readS32Le() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int readS32Ne() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long readS64Be() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long readS64Le() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long readS64Ne() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int readU24Be() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int readU24Le() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int readU24Ne() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long readU48Be() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long readU48Le() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long readU48Ne() {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void skip(final int length) {
        if (length != 0) {
            throw new IndexOutOfBoundsException();
        }
    }
}
