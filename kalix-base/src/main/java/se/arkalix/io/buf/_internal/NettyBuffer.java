package se.arkalix.io.buf._internal;

import io.netty.buffer.ByteBuf;
import se.arkalix.io.buf.Buffer;
import se.arkalix.io.buf.BufferReader;
import se.arkalix.io.buf.BufferWriter;
import se.arkalix.util._internal.BinaryMath;
import se.arkalix.util.annotation.Internal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

@Internal
public class NettyBuffer extends CheckedBuffer {
    private final ByteBuf buf;

    public NettyBuffer(final ByteBuf buf) {
        this.buf = Objects.requireNonNull(buf, "buf");
    }

    @Override
    public ByteBuffer asByteBuffer() {
        return buf.nioBuffer();
    }

    @Override
    protected Buffer copyUnchecked(final int offset, final int length) {
        return new NettyBuffer(buf.copy(offset, length));
    }

    @Override
    protected Buffer dupeUnchecked() {
        return new NettyBuffer(buf.slice());
    }

    @Override
    protected void getAtUnchecked(
        final int offset,
        final byte[] destination,
        final int destinationOffset,
        final int length
    ) {
        buf.getBytes(offset, destination, destinationOffset, length);
    }

    @Override
    protected void getAtUnchecked(
        final int offset,
        final BufferWriter destination,
        final int destinationOffset,
        final int length
    ) {
        buf.getBytes(offset, destination.asByteBuffer()
            .position(destinationOffset)
            .limit(destinationOffset + length));
    }

    @Override
    protected void getAtUnchecked(final int offset, final ByteBuffer destination) {
        buf.getBytes(offset, destination);
    }

    @Override
    protected byte getS8AtUnchecked(final int offset) {
        return buf.getByte(offset);
    }

    @Override
    protected short getS16BeAtUnchecked(final int offset) {
        return buf.getShort(offset);
    }

    @Override
    protected short getS16LeAtUnchecked(final int offset) {
        return buf.getShortLE(offset);
    }

    @Override
    protected short getS16NeAtUnchecked(final int offset) {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
            ? buf.getShortLE(offset)
            : buf.getShort(offset);
    }

    @Override
    protected int getS32BeAtUnchecked(final int offset) {
        return buf.getInt(offset);
    }

    @Override
    protected int getS32LeAtUnchecked(final int offset) {
        return buf.getIntLE(offset);
    }

    @Override
    protected int getS32NeAtUnchecked(final int offset) {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
            ? buf.getIntLE(offset)
            : buf.getInt(offset);
    }

    @Override
    protected long getS64BeAtUnchecked(final int offset) {
        return buf.getLong(offset);
    }

    @Override
    protected long getS64LeAtUnchecked(final int offset) {
        return buf.getLongLE(offset);
    }

    @Override
    protected long getS64NeAtUnchecked(final int offset) {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
            ? buf.getLongLE(offset)
            : buf.getLong(offset);
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
        buf.setIndex(readOffset, writeOffset);
    }

    @Override
    protected void onClose() {
        buf.release();
    }

    @Override
    public int readOffset() {
        return buf.readerIndex();
    }

    @Override
    protected void readOffsetUnchecked(final int readOffset) {
        buf.setIndex(readOffset, writeOffset());
    }

    @Override
    protected void readUnchecked(final byte[] destination, final int destinationOffset, final int length) {
        buf.readBytes(destination, destinationOffset, length);
    }

    @Override
    protected void readUnchecked(final BufferWriter destination, final int destinationOffset, final int length) {
        buf.readBytes(destination.asByteBuffer()
            .position(destinationOffset)
            .limit(destinationOffset + length));
    }

    @Override
    protected void readUnchecked(final ByteBuffer destination) {
        buf.readBytes(destination);
    }

    @Override
    protected byte readS8Unchecked() {
        return buf.readByte();
    }

    @Override
    protected short readS16BeUnchecked() {
        return buf.readShort();
    }

    @Override
    protected short readS16LeUnchecked() {
        return buf.readShortLE();
    }

    @Override
    protected short readS16NeUnchecked() {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
            ? buf.readShortLE()
            : buf.readShort();
    }

    @Override
    protected int readS32BeUnchecked() {
        return buf.readInt();
    }

    @Override
    protected int readS32LeUnchecked() {
        return buf.readIntLE();
    }

    @Override
    protected int readS32NeUnchecked() {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
            ? buf.readIntLE()
            : buf.readInt();
    }

    @Override
    protected long readS64BeUnchecked() {
        return buf.readLong();
    }

    @Override
    protected long readS64LeUnchecked() {
        return buf.readIntLE();
    }

    @Override
    protected long readS64NeUnchecked() {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
            ? buf.readLongLE()
            : buf.readLong();
    }

    @Override
    protected int readU24BeUnchecked() {
        final var buffer = new byte[3];
        readUnchecked(buffer, 0, 3);
        return BinaryMath.getU24BeAt(buffer, 0);
    }

    @Override
    protected int readU24LeUnchecked() {
        final var buffer = new byte[3];
        readUnchecked(buffer, 0, 3);
        return BinaryMath.getU24LeAt(buffer, 0);
    }

    @Override
    protected int readU24NeUnchecked() {
        final var buffer = new byte[3];
        readUnchecked(buffer, 0, 3);
        return BinaryMath.getU24NeAt(buffer, 0);
    }

    @Override
    protected long readU48BeUnchecked() {
        final var buffer = new byte[6];
        readUnchecked(buffer, 0, 6);
        return BinaryMath.getU48BeAt(buffer, 0);
    }

    @Override
    protected long readU48LeUnchecked() {
        final var buffer = new byte[6];
        readUnchecked(buffer, 0, 6);
        return BinaryMath.getU48LeAt(buffer, 0);
    }

    @Override
    protected long readU48NeUnchecked() {
        final var buffer = new byte[6];
        readUnchecked(buffer, 0, 6);
        return BinaryMath.getU48NeAt(buffer, 0);
    }

    @Override
    protected void setAtUnchecked(final int offset, final byte[] source, final int sourceOffset, final int length) {
        buf.setBytes(offset, source, sourceOffset, length);
    }

    @Override
    protected void setAtUnchecked(
        final int offset,
        final BufferReader source,
        final int sourceOffset,
        final int length
    ) {
        buf.setBytes(offset, source.asByteBuffer()
            .position(sourceOffset)
            .limit(sourceOffset + length));
    }

    @Override
    protected void setAtUnchecked(final int offset, final ByteBuffer source) {
        buf.setBytes(offset, source);
    }

    @Override
    protected void setS8AtUnchecked(final int offset, final byte value) {
        buf.setByte(offset, value);
    }

    @Override
    protected void setS16BeAtUnchecked(final int offset, final short value) {
        buf.setShort(offset, value);
    }

    @Override
    protected void setS16LeAtUnchecked(final int offset, final short value) {
        buf.setShortLE(offset, value);
    }

    @Override
    protected void setS16NeAtUnchecked(final int offset, final short value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            buf.setShortLE(offset, value);
        } else {
            buf.setShort(offset, value);
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
        buf.setInt(offset, value);
    }

    @Override
    protected void setS32LeAtUnchecked(final int offset, final int value) {
        buf.setIntLE(offset, value);
    }

    @Override
    protected void setS32NeAtUnchecked(final int offset, final int value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            buf.setIntLE(offset, value);
        } else {
            buf.setInt(offset, value);
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
        buf.setLong(offset, value);
    }

    @Override
    protected void setS64LeAtUnchecked(final int offset, final long value) {
        buf.setLongLE(offset, value);
    }

    @Override
    protected void setS64NeAtUnchecked(final int offset, final long value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            buf.setLongLE(offset, value);
        } else {
            buf.setLong(offset, value);
        }
    }

    @Override
    public int writeEnd() {
        return buf.capacity();
    }

    @Override
    public int writeEndMax() {
        return buf.maxCapacity();
    }

    @Override
    public int writeOffset() {
        return buf.writerIndex();
    }

    @Override
    protected void skipUnchecked(final int bytesToSkip) {
        buf.skipBytes(bytesToSkip);
    }

    @Override
    protected void writeEndUnchecked(final int writeEnd) {
        buf.capacity(writeEnd);
    }

    @Override
    protected void writeOffsetUnchecked(final int writeOffset) {
        buf.setIndex(buf.readerIndex(), writeOffset);
    }

    @Override
    protected void writeUnchecked(final byte[] source, final int sourceOffset, final int length) {
        buf.writeBytes(source, sourceOffset, length);
    }

    @Override
    protected void writeUnchecked(final BufferReader source, final int sourceOffset, final int length) {
        buf.writeBytes(source.asByteBuffer()
            .position(sourceOffset)
            .limit(sourceOffset + length));
    }

    @Override
    protected void writeUnchecked(final ByteBuffer source) {
        buf.writeBytes(source);
    }

    @Override
    protected void writeS8Unchecked(final byte value) {
        buf.writeByte(value);
    }

    @Override
    protected void writeS16BeUnchecked(final short value) {
        buf.writeShort(value);
    }

    @Override
    protected void writeS16LeUnchecked(final short value) {
        buf.writeShortLE(value);
    }

    @Override
    protected void writeS16NeUnchecked(final short value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            buf.writeShortLE(value);
        } else {
            buf.writeShort(value);
        }
    }

    @Override
    protected void writeS24BeUnchecked(final int value) {
        final var buffer = new byte[3];
        BinaryMath.setS24BeAt(buffer, 0, value);
        writeUnchecked(buffer, 0, 3);
    }

    @Override
    protected void writeS24LeUnchecked(final int value) {
        final var buffer = new byte[3];
        BinaryMath.setS24LeAt(buffer, 0, value);
        writeUnchecked(buffer, 0, 3);
    }

    @Override
    protected void writeS24NeUnchecked(final int value) {
        final var buffer = new byte[3];
        BinaryMath.setS24NeAt(buffer, 0, value);
        writeUnchecked(buffer, 0, 3);
    }

    @Override
    protected void writeS32BeUnchecked(final int value) {
        buf.writeInt(value);
    }

    @Override
    protected void writeS32LeUnchecked(final int value) {
        buf.writeIntLE(value);
    }

    @Override
    protected void writeS32NeUnchecked(final int value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            buf.writeIntLE(value);
        } else {
            buf.writeInt(value);
        }
    }

    @Override
    protected void writeS48BeUnchecked(final long value) {
        final var buffer = new byte[6];
        BinaryMath.setS48BeAt(buffer, 0, value);
        writeUnchecked(buffer, 0, 6);
    }

    @Override
    protected void writeS48LeUnchecked(final long value) {
        final var buffer = new byte[6];
        BinaryMath.setS48LeAt(buffer, 0, value);
        writeUnchecked(buffer, 0, 6);
    }

    @Override
    protected void writeS48NeUnchecked(final long value) {
        final var buffer = new byte[6];
        BinaryMath.setS48NeAt(buffer, 0, value);
        writeUnchecked(buffer, 0, 6);
    }

    @Override
    protected void writeS64BeUnchecked(final long value) {
        buf.writeLong(value);
    }

    @Override
    protected void writeS64LeUnchecked(final long value) {
        buf.writeLongLE(value);
    }

    @Override
    protected void writeS64NeUnchecked(final long value) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            buf.writeLongLE(value);
        } else {
            buf.writeLong(value);
        }
    }
}
