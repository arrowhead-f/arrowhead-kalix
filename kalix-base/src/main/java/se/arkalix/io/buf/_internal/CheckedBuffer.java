package se.arkalix.io.buf._internal;

import se.arkalix.io.buf.*;
import se.arkalix.util._internal.BinaryMath;
import se.arkalix.util.annotation.Internal;

import java.nio.ByteBuffer;

@Internal
public abstract class CheckedBuffer implements Buffer {
    private boolean isClosed = false;

    @Override
    public void clear() {
        offsetsUnchecked(0, 0);
    }

    @Override
    public void close() {
        isClosed = true;
        onClose();
    }

    @Override
    public Buffer copy(final int offset, final int length) {
        checkIfOpen();
        checkCopyRange(offset, length);
        return copyUnchecked(offset, length);
    }

    protected abstract Buffer copyUnchecked(final int offset, final int length);

    @Override
    public final Buffer dupe() {
        checkIfOpen();
        return dupeUnchecked();
    }

    protected abstract Buffer dupeUnchecked();

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void offsets(final int readOffset, final int writeOffset) {
        checkOffsets(readOffset, writeOffset, writeEnd());
        offsetsUnchecked(readOffset, writeOffset);
    }

    protected abstract void offsetsUnchecked(int readOffset, int writeOffset);

    protected abstract void onClose();

    @Override
    public BufferReader asReader() {
        checkIfOpen();
        return Buffer.super.asReader();
    }

    @Override
    public BufferWriter asWriter() {
        checkIfOpen();
        return Buffer.super.asWriter();
    }

    @Override
    public int readableBytes() {
        return writeOffset() - readOffset();
    }

    @Override
    public void readOffset(final int readOffset) {
        checkOffsets(readOffset, writeOffset(), writeEnd());
        readOffsetUnchecked(readOffset);
    }

    protected abstract void readOffsetUnchecked(int readOffset);

    @Override
    public void getAt(final int offset, final byte[] destination, final int destinationOffset, final int length) {
        checkIfOpen();
        if (destination == null) {
            throw new NullPointerException("destination");
        }
        checkWriteRange(destinationOffset, length, destination.length);
        checkReadRange(offset, length);
        getAtUnchecked(offset, destination, destinationOffset, length);
    }

    protected abstract void getAtUnchecked(int offset, byte[] destination, int destinationOffset, int length);

    @Override
    public void getAt(final int offset, final BufferWriter destination, final int destinationOffset, final int length) {
        checkIfOpen();
        if (destination == null) {
            throw new NullPointerException("destination");
        }
        checkReadRange(offset, length);
        getAtUnchecked(offset, destination, destinationOffset, length);
    }

    protected abstract void getAtUnchecked(int offset, BufferWriter destination, int destinationOffset, int length);

    @Override
    public void getAt(final int offset, final ByteBuffer destination) {
        checkIfOpen();
        if (destination == null) {
            throw new NullPointerException("destination");
        }
        checkReadRange(offset, destination.remaining());
        getAtUnchecked(offset, destination);
    }

    protected abstract void getAtUnchecked(int offset, ByteBuffer destination);

    @Override
    public byte getS8At(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 1);
        return getS8AtUnchecked(offset);
    }

    protected abstract byte getS8AtUnchecked(int offset);

    @Override
    public short getS16BeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 2);
        return getS16BeAtUnchecked(offset);
    }

    protected abstract short getS16BeAtUnchecked(int offset);

    @Override
    public short getS16LeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 2);
        return getS16LeAtUnchecked(offset);
    }

    protected abstract short getS16LeAtUnchecked(int offset);

    @Override
    public short getS16NeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 2);
        return getS16NeAtUnchecked(offset);
    }

    protected abstract short getS16NeAtUnchecked(int offset);

    @Override
    public int getS32BeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 4);
        return getS32BeAtUnchecked(offset);
    }

    protected abstract int getS32BeAtUnchecked(int offset);

    @Override
    public int getS32LeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 4);
        return getS32LeAtUnchecked(offset);
    }

    protected abstract int getS32LeAtUnchecked(int offset);

    @Override
    public int getS32NeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 4);
        return getS32NeAtUnchecked(offset);
    }

    protected abstract int getS32NeAtUnchecked(int offset);

    @Override
    public long getS64BeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 8);
        return getS64BeAtUnchecked(offset);
    }

    protected abstract long getS64BeAtUnchecked(int offset);

    @Override
    public long getS64LeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 8);
        return getS64LeAtUnchecked(offset);
    }

    protected abstract long getS64LeAtUnchecked(int offset);

    @Override
    public long getS64NeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 8);
        return getS64NeAtUnchecked(offset);
    }

    protected abstract long getS64NeAtUnchecked(int offset);

    @Override
    public int getU24BeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 3);
        return getU24BeAtUnchecked(offset);
    }

    protected abstract int getU24BeAtUnchecked(int offset);

    @Override
    public int getU24LeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 3);
        return getU24LeAtUnchecked(offset);
    }

    protected abstract int getU24LeAtUnchecked(int offset);

    @Override
    public int getU24NeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 3);
        return getU24NeAtUnchecked(offset);
    }

    protected abstract int getU24NeAtUnchecked(int offset);

    @Override
    public long getU48BeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 6);
        return getU48BeAtUnchecked(offset);
    }

    protected abstract long getU48BeAtUnchecked(int offset);

    @Override
    public long getU48LeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 6);
        return getU48LeAtUnchecked(offset);
    }

    protected abstract long getU48LeAtUnchecked(int offset);

    @Override
    public long getU48NeAt(final int offset) {
        checkIfOpen();
        checkReadRange(offset, 6);
        return getU48NeAtUnchecked(offset);
    }

    protected abstract long getU48NeAtUnchecked(int offset);

    @Override
    public void read(final byte[] destination, final int destinationOffset, final int length) {
        checkIfOpen();
        if (destination == null) {
            throw new NullPointerException("destination");
        }
        checkWriteRange(destinationOffset, length, destination.length);
        checkReadLength(length);
        readUnchecked(destination, destinationOffset, length);
    }

    protected abstract void readUnchecked(byte[] destination, int destinationOffset, int length);

    @Override
    public void read(final BufferWriter destination, final int destinationOffset, final int length) {
        checkIfOpen();
        if (destination == null) {
            throw new NullPointerException("destination");
        }
        checkReadLength(length);
        readUnchecked(destination, destinationOffset, length);
    }

    protected abstract void readUnchecked(BufferWriter destination, int destinationOffset, int length);

    @Override
    public void read(final ByteBuffer destination) {
        checkIfOpen();
        if (destination == null) {
            throw new NullPointerException("destination");
        }
        final var remaining = destination.remaining();
        checkReadLength(remaining);
        readUnchecked(destination);
    }

    protected abstract void readUnchecked(ByteBuffer destination);

    @Override
    public byte readS8() {
        checkIfOpen();
        checkReadLength(1);
        return readS8Unchecked();
    }

    protected abstract byte readS8Unchecked();

    @Override
    public short readS16Be() {
        checkIfOpen();
        checkReadLength(2);
        return readS16BeUnchecked();
    }

    protected abstract short readS16BeUnchecked();

    @Override
    public short readS16Le() {
        checkIfOpen();
        checkReadLength(2);
        return readS16LeUnchecked();
    }

    protected abstract short readS16LeUnchecked();

    @Override
    public short readS16Ne() {
        checkIfOpen();
        checkReadLength(2);
        return readS16NeUnchecked();
    }

    protected abstract short readS16NeUnchecked();

    @Override
    public int readS32Be() {
        checkIfOpen();
        checkReadLength(4);
        return readS32BeUnchecked();
    }

    protected abstract int readS32BeUnchecked();

    @Override
    public int readS32Le() {
        checkIfOpen();
        checkReadLength(4);
        return readS32LeUnchecked();
    }

    protected abstract int readS32LeUnchecked();

    @Override
    public int readS32Ne() {
        checkIfOpen();
        checkReadLength(4);
        return readS32NeUnchecked();
    }

    protected abstract int readS32NeUnchecked();

    @Override
    public long readS64Be() {
        checkIfOpen();
        checkReadLength(8);
        return readS64BeUnchecked();
    }

    protected abstract long readS64BeUnchecked();

    @Override
    public long readS64Le() {
        checkIfOpen();
        checkReadLength(8);
        return readS64LeUnchecked();
    }

    protected abstract long readS64LeUnchecked();

    @Override
    public long readS64Ne() {
        checkIfOpen();
        checkReadLength(8);
        return readS64NeUnchecked();
    }

    protected abstract long readS64NeUnchecked();

    @Override
    public int readU24Be() {
        checkIfOpen();
        checkReadLength(3);
        return readU24BeUnchecked();
    }

    protected abstract int readU24BeUnchecked();

    @Override
    public int readU24Le() {
        checkIfOpen();
        checkReadLength(3);
        return readU24LeUnchecked();
    }

    protected abstract int readU24LeUnchecked();

    @Override
    public int readU24Ne() {
        checkIfOpen();
        checkReadLength(3);
        return readU24NeUnchecked();
    }

    protected abstract int readU24NeUnchecked();

    @Override
    public long readU48Be() {
        checkIfOpen();
        checkReadLength(6);
        return readU48BeUnchecked();
    }

    protected abstract long readU48BeUnchecked();

    @Override
    public long readU48Le() {
        checkIfOpen();
        checkReadLength(6);
        return readU48LeUnchecked();
    }

    protected abstract long readU48LeUnchecked();

    @Override
    public long readU48Ne() {
        checkIfOpen();
        checkReadLength(6);
        return readU48NeUnchecked();
    }

    protected abstract long readU48NeUnchecked();

    @Override
    public void skip(final int length) {
        checkIfOpen();
        checkReadLength(length);
        skipUnchecked(length);
    }

    protected abstract void skipUnchecked(int bytesToSkip);

    @Override
    public int writableBytes() {
        return writeEnd() - writeOffset();
    }

    @Override
    public void writeEnd(final int writeEnd) {
        checkIfOpen();
        if (writeEnd < 0 || writeEnd > writeEndMax()) {
            throw new IndexOutOfBoundsException();
        }
        writeEndUnchecked(writeEnd);
    }

    protected abstract void writeEndUnchecked(final int writeEnd);

    @Override
    public void writeOffset(final int writeOffset) {
        checkOffsets(readOffset(), writeOffset, writeEnd());
        writeOffsetUnchecked(writeOffset);
    }

    protected abstract void writeOffsetUnchecked(int writeOffset);

    @Override
    public void setAt(final int offset, final byte[] source, final int sourceOffset, final int length) {
        checkIfOpen();
        if (source == null) {
            throw new NullPointerException("source");
        }
        checkReadRange(sourceOffset, length, source.length);
        ensureWriteRange(offset, length);
        setAtUnchecked(offset, source, sourceOffset, length);
    }

    protected abstract void setAtUnchecked(int offset, byte[] source, int sourceOffset, int length);

    @Override
    public void setAt(final int offset, final BufferReader source, final int sourceOffset, final int length) {
        checkIfOpen();
        if (source == null) {
            throw new NullPointerException("source");
        }
        ensureWriteRange(offset, length);
        setAtUnchecked(offset, source, sourceOffset, length);
    }

    protected abstract void setAtUnchecked(int offset, BufferReader source, int sourceOffset, int length);

    @Override
    public void setAt(final int offset, final ByteBuffer source) {
        checkIfOpen();
        if (source == null) {
            throw new NullPointerException("source");
        }
        ensureWriteRange(offset, source.remaining());
        setAtUnchecked(offset, source);
    }

    protected abstract void setAtUnchecked(int offset, ByteBuffer source);

    @Override
    public void setS8At(final int offset, final byte value) {
        checkIfOpen();
        ensureWriteRange(offset, 1);
        setS8AtUnchecked(offset, value);
    }

    protected abstract void setS8AtUnchecked(final int offset, final byte value);

    @Override
    public void setS16BeAt(final int offset, final short value) {
        checkIfOpen();
        ensureWriteRange(offset, 2);
        setS16BeAtUnchecked(offset, value);
    }

    protected abstract void setS16BeAtUnchecked(int offset, short value);

    @Override
    public void setS16LeAt(final int offset, final short value) {
        checkIfOpen();
        ensureWriteRange(offset, 2);
        setS16LeAtUnchecked(offset, value);
    }

    protected abstract void setS16LeAtUnchecked(int offset, short value);

    @Override
    public void setS16NeAt(final int offset, final short value) {
        checkIfOpen();
        ensureWriteRange(offset, 2);
        setS16NeAtUnchecked(offset, value);
    }

    protected abstract void setS16NeAtUnchecked(int offset, short value);

    @Override
    public void setS24BeAt(final int offset, final int value) {
        checkS24(value);
        checkIfOpen();
        ensureWriteRange(offset, 3);
        setS24BeAtUnchecked(offset, value);
    }

    protected abstract void setS24BeAtUnchecked(int offset, int value);

    @Override
    public void setS24LeAt(final int offset, final int value) {
        checkS24(value);
        checkIfOpen();
        ensureWriteRange(offset, 3);
        setS24LeAtUnchecked(offset, value);
    }

    protected abstract void setS24LeAtUnchecked(int offset, int value);

    @Override
    public void setS24NeAt(final int offset, final int value) {
        checkS24(value);
        checkIfOpen();
        ensureWriteRange(offset, 3);
        setS24NeAtUnchecked(offset, value);
    }

    protected abstract void setS24NeAtUnchecked(int offset, int value);

    @Override
    public void setS32BeAt(final int offset, final int value) {
        checkIfOpen();
        ensureWriteRange(offset, 4);
        setS32BeAtUnchecked(offset, value);
    }

    protected abstract void setS32BeAtUnchecked(int offset, int value);

    @Override
    public void setS32LeAt(final int offset, final int value) {
        checkIfOpen();
        ensureWriteRange(offset, 4);
        setS32LeAtUnchecked(offset, value);
    }

    protected abstract void setS32LeAtUnchecked(int offset, int value);

    @Override
    public void setS32NeAt(final int offset, final int value) {
        checkIfOpen();
        ensureWriteRange(offset, 4);
        setS32NeAtUnchecked(offset, value);
    }

    protected abstract void setS32NeAtUnchecked(int offset, int value);

    @Override
    public void setS48BeAt(final int offset, final long value) {
        checkS48(value);
        checkIfOpen();
        ensureWriteRange(offset, 6);
        setS48BeAtUnchecked(offset, value);
    }

    protected abstract void setS48BeAtUnchecked(int offset, long value);

    @Override
    public void setS48LeAt(final int offset, final long value) {
        checkS48(value);
        checkIfOpen();
        ensureWriteRange(offset, 6);
        setS48LeAtUnchecked(offset, value);
    }

    protected abstract void setS48LeAtUnchecked(int offset, long value);

    @Override
    public void setS48NeAt(final int offset, final long value) {
        checkS48(value);
        checkIfOpen();
        ensureWriteRange(offset, 6);
        setS48NeAtUnchecked(offset, value);
    }

    protected abstract void setS48NeAtUnchecked(int offset, long value);

    @Override
    public void setS64BeAt(final int offset, final long value) {
        checkIfOpen();
        ensureWriteRange(offset, 8);
        setS64BeAtUnchecked(offset, value);
    }

    protected abstract void setS64BeAtUnchecked(int offset, long value);

    @Override
    public void setS64LeAt(final int offset, final long value) {
        checkIfOpen();
        ensureWriteRange(offset, 8);
        setS64LeAtUnchecked(offset, value);
    }

    protected abstract void setS64LeAtUnchecked(int offset, long value);

    @Override
    public void setS64NeAt(final int offset, final long value) {
        checkIfOpen();
        ensureWriteRange(offset, 8);
        setS64NeAtUnchecked(offset, value);
    }

    protected abstract void setS64NeAtUnchecked(int offset, long value);

    @Override
    public void write(final byte[] source, final int sourceOffset, final int length) {
        checkIfOpen();
        if (source == null) {
            throw new NullPointerException("source");
        }
        checkReadRange(sourceOffset, length, source.length);
        ensureWriteLength(length);
        writeUnchecked(source, sourceOffset, length);
    }

    protected abstract void writeUnchecked(byte[] source, int sourceOffset, int length);

    @Override
    public void write(final BufferReader source, final int sourceOffset, final int length) {
        checkIfOpen();
        if (source == null) {
            throw new NullPointerException("source");
        }
        ensureWriteLength(length);
        writeUnchecked(source, sourceOffset, length);
    }

    protected abstract void writeUnchecked(BufferReader source, int sourceOffset, int length);

    @Override
    public void write(final ByteBuffer source) {
        checkIfOpen();
        if (source == null) {
            throw new NullPointerException("source");
        }
        final var remaining = source.remaining();
        ensureWriteLength(remaining);
        writeUnchecked(source);
    }

    protected abstract void writeUnchecked(ByteBuffer source);

    @Override
    public void writeS8(final byte value) {
        checkIfOpen();
        ensureWriteLength(1);
        writeS8Unchecked(value);
    }

    protected abstract void writeS8Unchecked(byte value);

    @Override
    public void writeS16Be(final short value) {
        checkIfOpen();
        ensureWriteLength(2);
        writeS16BeUnchecked(value);
    }

    protected abstract void writeS16BeUnchecked(short value);

    @Override
    public void writeS16Le(final short value) {
        checkIfOpen();
        ensureWriteLength(2);
        writeS16LeUnchecked(value);
    }

    protected abstract void writeS16LeUnchecked(short value);

    @Override
    public void writeS16Ne(final short value) {
        checkIfOpen();
        ensureWriteLength(2);
        writeS16NeUnchecked(value);
    }

    protected abstract void writeS16NeUnchecked(short value);

    @Override
    public void writeS24Be(final int value) {
        checkS24(value);
        checkIfOpen();
        ensureWriteLength(3);
        writeS24BeUnchecked(value);
    }

    protected abstract void writeS24BeUnchecked(int value);

    @Override
    public void writeS24Le(final int value) {
        checkS24(value);
        checkIfOpen();
        ensureWriteLength(3);
        writeS24LeUnchecked(value);
    }

    protected abstract void writeS24LeUnchecked(int value);

    @Override
    public void writeS24Ne(final int value) {
        checkS24(value);
        checkIfOpen();
        ensureWriteLength(3);
        writeS24NeUnchecked(value);
    }

    protected abstract void writeS24NeUnchecked(int value);

    @Override
    public void writeS32Be(final int value) {
        checkIfOpen();
        ensureWriteLength(4);
        writeS32BeUnchecked(value);
    }

    protected abstract void writeS32BeUnchecked(int value);

    @Override
    public void writeS32Le(final int value) {
        checkIfOpen();
        ensureWriteLength(4);
        writeS32LeUnchecked(value);
    }

    protected abstract void writeS32LeUnchecked(int value);

    @Override
    public void writeS32Ne(final int value) {
        checkIfOpen();
        ensureWriteLength(4);
        writeS32NeUnchecked(value);
    }

    protected abstract void writeS32NeUnchecked(int value);

    @Override
    public void writeS48Be(final long value) {
        checkS48(value);
        checkIfOpen();
        ensureWriteLength(6);
        writeS48BeUnchecked(value);
    }

    protected abstract void writeS48BeUnchecked(long value);

    @Override
    public void writeS48Le(final long value) {
        checkS48(value);
        checkIfOpen();
        ensureWriteLength(6);
        writeS48LeUnchecked(value);
    }

    protected abstract void writeS48LeUnchecked(long value);

    @Override
    public void writeS48Ne(final long value) {
        checkS48(value);
        checkIfOpen();
        ensureWriteLength(6);
        writeS48NeUnchecked(value);
    }

    protected abstract void writeS48NeUnchecked(long value);

    @Override
    public void writeS64Be(final long value) {
        checkIfOpen();
        ensureWriteLength(8);
        writeS64BeUnchecked(value);
    }

    protected abstract void writeS64BeUnchecked(long value);

    @Override
    public void writeS64Le(final long value) {
        checkIfOpen();
        ensureWriteLength(8);
        writeS64LeUnchecked(value);
    }

    protected abstract void writeS64LeUnchecked(long value);

    @Override
    public void writeS64Ne(final long value) {
        checkIfOpen();
        ensureWriteLength(8);
        writeS64NeUnchecked(value);
    }

    protected abstract void writeS64NeUnchecked(long value);

    protected void checkS24(final int value) {
        if (value < -8388608 || value > 8388607) {
            throw new BufferValueOutOfBounds(-8388608, 8388607, value);
        }
    }

    protected void checkS48(final long value) {
        if (value < -140737488355328L || value > 140737488355327L) {
            throw new BufferValueOutOfBounds(-140737488355328L, 140737488355327L, value);
        }
    }

    protected void checkIfOpen() {
        if (isClosed) {
            throw new BufferIsClosed();
        }
    }

    protected static void checkOffsets(final int readOffset, final int writeOffset, final int writeEnd) {
        if (readOffset < 0 || readOffset > writeOffset || writeOffset > writeEnd) {
            throw new IndexOutOfBoundsException();
        }
    }

    protected void checkCopyRange(final int copyOffset, final int length) {
        if (BinaryMath.isRangeOutOfBounds(copyOffset, length, writeEnd())) {
            throw new IndexOutOfBoundsException();
        }
    }

    protected void checkReadRange(final int readOffset, final int length) {
        if (BinaryMath.isRangeOutOfBounds(readOffset, length, writeOffset())) {
            throw new IndexOutOfBoundsException();
        }
    }

    protected static void checkReadRange(final int readOffset, final int length, final int readEnd) {
        if (BinaryMath.isRangeOutOfBounds(readOffset, length, readEnd)) {
            throw new IndexOutOfBoundsException();
        }
    }

    protected void checkReadLength(final int length) {
        if (readOffset() > writeOffset() - length) {
            throw new IndexOutOfBoundsException();
        }
    }

    protected static void checkWriteRange(final int writeOffset, final int length, final int writeEnd) {
        if (BinaryMath.isRangeOutOfBounds(writeOffset, length, writeEnd)) {
            throw new IndexOutOfBoundsException();
        }
    }

    protected void ensureWriteRange(final int writeOffset, final int length) {
        final var rangeEnd = writeOffset + length;
        if (rangeEnd <= writeEnd()) {
            return;
        }
        if (rangeEnd > writeEndMax()) {
            throw new IndexOutOfBoundsException();
        }
        writeEnd(rangeEnd);
    }

    protected void ensureWriteLength(final int length) {
        ensureWriteRange(writeOffset(), length);
    }
}
