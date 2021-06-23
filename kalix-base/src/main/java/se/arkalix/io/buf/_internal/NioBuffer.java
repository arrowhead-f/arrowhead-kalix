package se.arkalix.io.buf._internal;

import se.arkalix.io.buf.BufferIsClosed;
import se.arkalix.io.buf.BufferReader;
import se.arkalix.io.buf.BufferWriter;
import se.arkalix.util._internal.BinaryMath;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.annotation.Unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static se.arkalix.util._internal.BinaryMath.roundUpToMultipleOfPowerOfTwo;

@Internal
public class NioBuffer extends CheckedBuffer {
    private final int writeEnd;

    private ByteBuffer byteBuffer;
    private int readOffset;
    private int writeOffset;

    /**
     * The provided {@code byteBuffer} <b>must</b> never be used outside of
     * the created class instance.
     */
    @Unsafe
    public NioBuffer(final ByteBuffer byteBuffer, final int writeEnd) {
        if (byteBuffer == null) {
            throw new NullPointerException("byteBuffer");
        }
        if (writeEnd < 0) {
            throw new IndexOutOfBoundsException();
        }

        this.byteBuffer = byteBuffer.position(0)
            .limit(Math.min(byteBuffer.capacity(), writeEnd))
            .order(ByteOrder.BIG_ENDIAN);

        this.writeEnd = writeEnd;
    }

    @Override
    protected void getAtUnchecked(
        final int offset,
        final byte[] destination,
        final int destinationOffset,
        final int length
    ) {
        byteBuffer.asReadOnlyBuffer()
            .position(offset)
            .get(destination, destinationOffset, length);
    }

    @Override
    protected void getAtUnchecked(
        int offset,
        final BufferWriter destination,
        final int destinationOffset,
        final int length
    ) {
        destination.setAt(destinationOffset, byteBuffer.asReadOnlyBuffer()
            .position(offset)
            .limit(offset + length));
    }

    @Override
    protected void getAtUnchecked(final int offset, final ByteBuffer destination) {
        destination.put(byteBuffer.asReadOnlyBuffer()
            .position(offset)
            .limit(offset + destination.remaining()));
    }

    @Override
    protected byte getS8AtUnchecked(final int offset) {
        return byteBuffer.get(offset);
    }

    @Override
    protected short getS16BeAtUnchecked(final int offset) {
        return byteBuffer.getShort(offset);
    }

    @Override
    protected short getS16LeAtUnchecked(final int offset) {
        return Short.reverseBytes(byteBuffer.getShort(offset));
    }

    @Override
    protected short getS16NeAtUnchecked(final int offset) {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
            ? getS16LeAtUnchecked(offset)
            : getS16BeAtUnchecked(offset);
    }

    @Override
    protected int getS32BeAtUnchecked(final int offset) {
        return byteBuffer.getInt(offset);
    }

    @Override
    protected int getS32LeAtUnchecked(final int offset) {
        return Integer.reverseBytes(byteBuffer.getInt(offset));
    }

    @Override
    protected int getS32NeAtUnchecked(final int offset) {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
            ? getS32LeAtUnchecked(offset)
            : getS32BeAtUnchecked(offset);
    }

    @Override
    protected long getS64BeAtUnchecked(final int offset) {
        return byteBuffer.getLong(offset);
    }

    @Override
    protected long getS64LeAtUnchecked(final int offset) {
        return Long.reverseBytes(byteBuffer.getLong(offset));
    }

    @Override
    protected long getS64NeAtUnchecked(final int offset) {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
            ? getS64LeAtUnchecked(offset)
            : getS64BeAtUnchecked(offset);
    }

    @Override
    protected int getU24BeAtUnchecked(final int offset) {
        final var buffer = new byte[3];
        getAtUnchecked(offset, buffer, 0, 3);
        return BinaryMath.getU24BeAt(buffer, 0);
    }

    @Override
    protected int getU24LeAtUnchecked(final int offset) {
        final var buffer = new byte[3];
        getAtUnchecked(offset, buffer, 0, 3);
        return BinaryMath.getU24LeAt(buffer, 0);
    }

    @Override
    protected int getU24NeAtUnchecked(final int offset) {
        final var buffer = new byte[3];
        getAtUnchecked(offset, buffer, 0, 3);
        return BinaryMath.getU24NeAt(buffer, 0);
    }

    @Override
    protected long getU48BeAtUnchecked(final int offset) {
        final var buffer = new byte[6];
        getAtUnchecked(offset, buffer, 0, 6);
        return BinaryMath.getU48BeAt(buffer, 0);
    }

    @Override
    protected long getU48LeAtUnchecked(final int offset) {
        final var buffer = new byte[6];
        getAtUnchecked(offset, buffer, 0, 6);
        return BinaryMath.getU48LeAt(buffer, 0);
    }

    @Override
    protected long getU48NeAtUnchecked(final int offset) {
        final var buffer = new byte[6];
        getAtUnchecked(offset, buffer, 0, 6);
        return BinaryMath.getU48NeAt(buffer, 0);
    }

    @Override
    protected void offsetsUnchecked(final int readOffset, final int writeOffset) {
        this.readOffset = readOffset;
        this.writeOffset = writeOffset;
    }

    @Override
    protected void onClose() {
        byteBuffer = null;
    }

    @Override
    public int readOffset() {
        return readOffset;
    }

    @Override
    protected void readOffsetUnchecked(final int readOffset) {
        this.readOffset = readOffset;
    }

    @Override
    protected void readUnchecked(final byte[] destination, final int destinationOffset, final int length) {
        getAtUnchecked(readOffset, destination, destinationOffset, length);
        readOffset += length;
    }

    @Override
    protected void readUnchecked(final BufferWriter destination, final int destinationOffset, final int length) {
        getAtUnchecked(readOffset, destination, destinationOffset, length);
        readOffset += length;
    }

    @Override
    protected void readUnchecked(final ByteBuffer destination) {
        final var length = destination.remaining();
        getAtUnchecked(readOffset, destination);
        readOffset += length;
    }

    @Override
    protected byte readS8Unchecked() {
        final byte s8 = getS8AtUnchecked(readOffset);
        readOffset += 1;
        return s8;
    }

    @Override
    protected short readS16BeUnchecked() {
        final short s16 = getS16BeAtUnchecked(readOffset);
        readOffset += 2;
        return s16;
    }

    @Override
    protected short readS16LeUnchecked() {
        final short s16 = getS16LeAtUnchecked(readOffset);
        readOffset += 2;
        return s16;
    }

    @Override
    protected short readS16NeUnchecked() {
        final short s16 = getS16NeAtUnchecked(readOffset);
        readOffset += 2;
        return s16;
    }

    @Override
    protected int readU24BeUnchecked() {
        final int s24 = getS24BeAt(readOffset);
        readOffset += 3;
        return s24;
    }

    @Override
    protected int readU24LeUnchecked() {
        final int s24 = getS24LeAt(readOffset);
        readOffset += 3;
        return s24;
    }

    @Override
    protected int readU24NeUnchecked() {
        final int s24 = getS24NeAt(readOffset);
        readOffset += 3;
        return s24;
    }

    @Override
    protected int readS32BeUnchecked() {
        final int s32 = getS32BeAtUnchecked(readOffset);
        readOffset += 4;
        return s32;
    }

    @Override
    protected int readS32LeUnchecked() {
        final int s32 = getS32LeAtUnchecked(readOffset);
        readOffset += 4;
        return s32;
    }

    @Override
    protected int readS32NeUnchecked() {
        final int s32 = getS32NeAtUnchecked(readOffset);
        readOffset += 4;
        return s32;
    }

    @Override
    protected long readU48BeUnchecked() {
        final long s48 = getS48BeAt(readOffset);
        readOffset += 6;
        return s48;
    }

    @Override
    protected long readU48LeUnchecked() {
        final long s48 = getS48LeAt(readOffset);
        readOffset += 6;
        return s48;
    }

    @Override
    protected long readU48NeUnchecked() {
        final long s48 = getS48NeAt(readOffset);
        readOffset += 6;
        return s48;
    }

    @Override
    protected long readS64BeUnchecked() {
        final long s64 = getS64BeAtUnchecked(readOffset);
        readOffset += 8;
        return s64;
    }

    @Override
    protected long readS64LeUnchecked() {
        final long s64 = getS64LeAtUnchecked(readOffset);
        readOffset += 8;
        return s64;
    }

    @Override
    protected long readS64NeUnchecked() {
        final long s64 = getS64NeAtUnchecked(readOffset);
        readOffset += 8;
        return s64;
    }

    @Override
    protected void setAtUnchecked(final int offset, final byte[] source, final int sourceOffset, final int length) {
        byteBuffer.duplicate()
            .position(offset)
            .put(source, sourceOffset, length);
    }

    @Override
    protected void setAtUnchecked(
        final int offset,
        final BufferReader source,
        final int sourceOffset,
        final int length
    ) {
        source.getAt(sourceOffset, byteBuffer.duplicate()
            .position(offset)
            .limit(offset + length));
    }

    @Override
    protected void setAtUnchecked(final int offset, final ByteBuffer source) {
        byteBuffer.duplicate()
            .position(offset)
            .put(source);
    }

    @Override
    protected void setS8AtUnchecked(final int offset, final byte value) {
        byteBuffer.put(offset, value);
    }

    @Override
    protected void setS16BeAtUnchecked(final int offset, final short value) {
        byteBuffer.putShort(offset, value);
    }

    @Override
    protected void setS16LeAtUnchecked(final int offset, final short value) {
        byteBuffer.putShort(offset, Short.reverseBytes(value));
    }

    @Override
    protected void setS16NeAtUnchecked(final int offset, final short value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            setS16LeAtUnchecked(offset, value);
        }
        else {
            setS16BeAtUnchecked(offset, value);
        }
    }

    @Override
    protected void setS24BeAtUnchecked(final int offset, final int value) {
        final var buffer = new byte[3];
        BinaryMath.setS24BeAt(buffer, 0, value);
        setAtUnchecked(offset, buffer, 0, 3);
    }

    @Override
    protected void setS24LeAtUnchecked(final int offset, final int value) {
        final var buffer = new byte[3];
        BinaryMath.setS24LeAt(buffer, 0, value);
        setAtUnchecked(offset, buffer, 0, 3);
    }

    @Override
    protected void setS24NeAtUnchecked(final int offset, final int value) {
        final var buffer = new byte[3];
        BinaryMath.setS24NeAt(buffer, 0, value);
        setAtUnchecked(offset, buffer, 0, 3);
    }

    @Override
    protected void setS32BeAtUnchecked(final int offset, final int value) {
        byteBuffer.putInt(offset, value);
    }

    @Override
    protected void setS32LeAtUnchecked(final int offset, final int value) {
        byteBuffer.putInt(offset, Integer.reverseBytes(value));
    }

    @Override
    protected void setS32NeAtUnchecked(final int offset, final int value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            setS32LeAtUnchecked(offset, value);
        }
        else {
            setS32BeAtUnchecked(offset, value);
        }
    }

    @Override
    protected void setS48BeAtUnchecked(final int offset, final long value) {
        final var buffer = new byte[6];
        BinaryMath.setS48BeAt(buffer, 0, value);
        setAtUnchecked(offset, buffer, 0, 6);
    }

    @Override
    protected void setS48LeAtUnchecked(final int offset, final long value) {
        final var buffer = new byte[6];
        BinaryMath.setS48LeAt(buffer, 0, value);
        setAtUnchecked(offset, buffer, 0, 6);
    }

    @Override
    protected void setS48NeAtUnchecked(final int offset, final long value) {
        final var buffer = new byte[6];
        BinaryMath.setS48NeAt(buffer, 0, value);
        setAtUnchecked(offset, buffer, 0, 6);
    }

    @Override
    protected void setS64BeAtUnchecked(final int offset, final long value) {
        byteBuffer.putLong(offset, value);
    }

    @Override
    protected void setS64LeAtUnchecked(final int offset, final long value) {
        byteBuffer.putLong(offset, Long.reverseBytes(value));
    }

    @Override
    protected void setS64NeAtUnchecked(final int offset, final long value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            setS64LeAtUnchecked(offset, value);
        }
        else {
            setS64BeAtUnchecked(offset, value);
        }
    }

    @Override
    public int writeEnd() {
        return writeEnd;
    }

    @Override
    public int writeOffset() {
        return writeOffset;
    }

    @Override
    protected void skipUnchecked(final int bytesToSkip) {
        writeOffset += bytesToSkip;
    }

    @Override
    protected void writeOffsetUnchecked(final int writeOffset) {
        this.writeOffset = writeOffset;
    }

    @Override
    protected void writeUnchecked(final byte[] source, final int sourceOffset, final int length) {
        setAtUnchecked(writeOffset, source, sourceOffset, length);
        writeOffset += length;
    }

    @Override
    protected void writeUnchecked(final BufferReader source, final int sourceOffset, final int length) {
        setAtUnchecked(writeOffset, source, sourceOffset, length);
        writeOffset += length;
    }

    @Override
    protected void writeUnchecked(final ByteBuffer source) {
        final var length = source.remaining();
        setAtUnchecked(writeOffset, source);
        writeOffset += length;
    }

    @Override
    protected void writeS8Unchecked(final byte value) {
        setS8AtUnchecked(writeOffset, value);
        writeOffset += 1;
    }

    @Override
    protected void writeS16BeUnchecked(final short value) {
        setS16BeAtUnchecked(writeOffset, value);
        writeOffset += 2;
    }

    @Override
    protected void writeS16LeUnchecked(final short value) {
        setS16LeAtUnchecked(writeOffset, value);
        writeOffset += 2;
    }

    @Override
    protected void writeS16NeUnchecked(final short value) {
        setS16NeAtUnchecked(writeOffset, value);
        writeOffset += 2;
    }

    @Override
    protected void writeS24BeUnchecked(final int value) {
        setS24BeAtUnchecked(writeOffset, value);
        writeOffset += 3;
    }

    @Override
    protected void writeS24LeUnchecked(final int value) {
        setS24LeAtUnchecked(writeOffset, value);
        writeOffset += 3;
    }

    @Override
    protected void writeS24NeUnchecked(final int value) {
        setS24NeAtUnchecked(writeOffset, value);
        writeOffset += 3;
    }

    @Override
    protected void writeS32BeUnchecked(final int value) {
        setS32BeAtUnchecked(writeOffset, value);
        writeOffset += 4;
    }

    @Override
    protected void writeS32LeUnchecked(final int value) {
        setS32LeAtUnchecked(writeOffset, value);
        writeOffset += 4;
    }

    @Override
    protected void writeS32NeUnchecked(final int value) {
        setS32LeAtUnchecked(writeOffset, value);
        writeOffset += 4;
    }

    @Override
    protected void writeS48BeUnchecked(final long value) {
        setS48BeAtUnchecked(writeOffset, value);
        writeOffset += 6;
    }

    @Override
    protected void writeS48LeUnchecked(final long value) {
        setS48LeAtUnchecked(writeOffset, value);
        writeOffset += 6;
    }

    @Override
    protected void writeS48NeUnchecked(final long value) {
        setS48NeAtUnchecked(writeOffset, value);
        writeOffset += 6;
    }

    @Override
    protected void writeS64BeUnchecked(final long value) {
        setS64BeAtUnchecked(writeOffset, value);
        writeOffset += 8;
    }

    @Override
    protected void writeS64LeUnchecked(final long value) {
        setS64LeAtUnchecked(writeOffset, value);
        writeOffset += 8;
    }

    @Override
    protected void writeS64NeUnchecked(final long value) {
        setS64NeAtUnchecked(writeOffset, value);
        writeOffset += 8;
    }

    @Override
    protected int capacity() {
        if (byteBuffer == null) {
            throw new BufferIsClosed();
        }
        return byteBuffer.limit();
    }

    @Override
    protected void capacity(int capacity) {
        if (capacity > byteBuffer.capacity()) {
            capacity = roundUpToMultipleOfPowerOfTwo(capacity, 4096)
                .orElse(capacity);
            byteBuffer = (byteBuffer.isDirect()
                ? ByteBuffer.allocateDirect(capacity)
                : ByteBuffer.allocate(capacity))
                .put(byteBuffer.position(0)
                    .limit(writeOffset()))
                .clear();
        }
        else if (capacity < writeOffset) {
            readOffset = Math.min(readOffset, capacity);
            writeOffset = capacity;
        }
        byteBuffer.limit(capacity);
    }
}
