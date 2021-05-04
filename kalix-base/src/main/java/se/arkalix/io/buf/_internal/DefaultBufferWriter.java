package se.arkalix.io.buf._internal;

import se.arkalix.io.buf.BufferReader;
import se.arkalix.io.buf.BufferWriter;
import se.arkalix.util.annotation.Internal;

import java.nio.ByteBuffer;
import java.util.Objects;

@Internal
public class DefaultBufferWriter implements BufferWriter {
    private BufferWriter inner;

    public DefaultBufferWriter(final BufferWriter inner) {
        this.inner = Objects.requireNonNull(inner, "inner");
    }

    @Override
    public ByteBuffer asByteBuffer() {
        return inner.asByteBuffer();
    }

    @Override
    public void setAt(final int offset, final byte[] source, final int sourceOffset, final int length) {
        inner.setAt(offset, source, sourceOffset, length);
    }

    @Override
    public void setAt(final int offset, final BufferReader source, final int sourceOffset, final int length) {
        inner.setAt(offset, source, sourceOffset, length);
    }

    @Override
    public void setAt(final int offset, final ByteBuffer source) {
        inner.setAt(offset, source);
    }

    @Override
    public void setS8At(final int offset, final byte value) {
        inner.setS8At(offset, value);
    }

    @Override
    public void setS16BeAt(final int offset, final short value) {
        inner.setS16BeAt(offset, value);
    }

    @Override
    public void setS16NeAt(final int offset, final short value) {
        inner.setS16NeAt(offset, value);
    }

    @Override
    public void setS16LeAt(final int offset, final short value) {
        inner.setS16LeAt(offset, value);
    }

    @Override
    public void setS24NeAt(final int offset, final int value) {
        inner.setS24NeAt(offset, value);
    }

    @Override
    public void setS24BeAt(final int offset, final int value) {
        inner.setS24BeAt(offset, value);
    }

    @Override
    public void setS24LeAt(final int offset, final int value) {
        inner.setS24LeAt(offset, value);
    }

    @Override
    public void setS32BeAt(final int offset, final int value) {
        inner.setS32BeAt(offset, value);
    }

    @Override
    public void setS32NeAt(final int offset, final int value) {
        inner.setS32NeAt(offset, value);
    }

    @Override
    public void setS32LeAt(final int offset, final int value) {
        inner.setS32LeAt(offset, value);
    }

    @Override
    public void setS48NeAt(final int offset, final long value) {
        inner.setS48NeAt(offset, value);
    }

    @Override
    public void setS48BeAt(final int offset, final long value) {
        inner.setS48BeAt(offset, value);
    }

    @Override
    public void setS48LeAt(final int offset, final long value) {
        inner.setS48LeAt(offset, value);
    }

    @Override
    public void setS64BeAt(final int offset, final long value) {
        inner.setS64BeAt(offset, value);
    }

    @Override
    public void setS64NeAt(final int offset, final long value) {
        inner.setS64NeAt(offset, value);
    }

    @Override
    public void setS64LeAt(final int offset, final long value) {
        inner.setS64LeAt(offset, value);
    }

    @Override
    public int writableBytes() {
        return inner.writableBytes();
    }

    @Override
    public void writableBytes(final int writableBytes) {
        inner.writableBytes(writableBytes);
    }

    @Override
    public int writeEnd() {
        return inner.writeEnd();
    }

    @Override
    public void writeEnd(final int writeEnd) {
        inner.writeEnd(writeEnd);
    }

    @Override
    public int writeEndMax() {
        return inner.writeEndMax();
    }

    @Override
    public int writeOffset() {
        return inner.writeOffset();
    }

    @Override
    public void writeOffset(final int writeOffset) {
        inner.writeOffset(writeOffset);
    }

    @Override
    public void write(final byte[] source, final int sourceOffset, final int length) {
        inner.write(source, sourceOffset, length);
    }

    @Override
    public void write(final BufferReader source, final int sourceOffset, final int length) {
        inner.write(source, sourceOffset, length);
    }

    @Override
    public void write(final ByteBuffer source) {
        inner.write(source);
    }

    @Override
    public void writeS8(final byte value) {
        inner.writeS8(value);
    }

    @Override
    public void writeS16Be(final short value) {
        inner.writeS16Be(value);
    }

    @Override
    public void writeS16Le(final short value) {
        inner.writeS16Le(value);
    }

    @Override
    public void writeS16Ne(final short value) {
        inner.writeS16Ne(value);
    }

    @Override
    public void writeS24Ne(final int value) {
        inner.writeS24Ne(value);
    }

    @Override
    public void writeS32Be(final int value) {
        inner.writeS32Be(value);
    }

    @Override
    public void writeS32Le(final int value) {
        inner.writeS32Le(value);
    }

    @Override
    public void writeS24Be(final int value) {
        inner.writeS24Be(value);
    }

    @Override
    public void writeS24Le(final int value) {
        inner.writeS24Le(value);
    }

    @Override
    public void writeS32Ne(final int value) {
        inner.writeS32Ne(value);
    }

    @Override
    public void writeS48Ne(final long value) {
        inner.writeS48Ne(value);
    }

    @Override
    public void writeS64Be(final long value) {
        inner.writeS64Be(value);
    }

    @Override
    public void writeS64Le(final long value) {
        inner.writeS64Le(value);
    }

    @Override
    public void writeS48Be(final long value) {
        inner.writeS48Be(value);
    }

    @Override
    public void writeS48Le(final long value) {
        inner.writeS48Le(value);
    }

    @Override
    public void writeS64Ne(final long value) {
        inner.writeS64Ne(value);
    }

    @Override
    public boolean isClosed() {
        return inner.isClosed();
    }

    @Override
    public void close() {
        inner = ClosedBuffer.instance();
    }
}
