package se.arkalix.io.buf._internal;

import se.arkalix.io.buf.BufferReader;
import se.arkalix.io.buf.BufferWriter;
import se.arkalix.util.annotation.Internal;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

@Internal
public class DefaultBufferReader implements BufferReader {
    private BufferReader inner;

    public DefaultBufferReader(final BufferReader inner) {
        this.inner = Objects.requireNonNull(inner, "inner");
    }

    @Override
    public void getAt(final int offset, final byte[] destination, final int destinationOffset, final int length) {
        inner.getAt(offset, destination, destinationOffset, length);
    }

    @Override
    public void getAt(final int offset, final BufferWriter destination, final int destinationOffset, final int length) {
        inner.getAt(offset, destination, destinationOffset, length);
    }

    @Override
    public void getAt(final int offset, final ByteBuffer destination) {
        inner.getAt(offset, destination);
    }

    @Override
    public int getAt(final int offset, final WritableByteChannel destination, final int maxLength) {
        return inner.getAt(offset, destination, maxLength);
    }

    @Override
    public byte getS8At(final int offset) {
        return inner.getS8At(offset);
    }

    @Override
    public short getS16BeAt(final int offset) {
        return inner.getS16BeAt(offset);
    }

    @Override
    public short getS16LeAt(final int offset) {
        return inner.getS16LeAt(offset);
    }

    @Override
    public short getS16NeAt(final int offset) {
        return inner.getS16NeAt(offset);
    }

    @Override
    public int getS32BeAt(final int offset) {
        return inner.getS32BeAt(offset);
    }

    @Override
    public int getS32LeAt(final int offset) {
        return inner.getS32LeAt(offset);
    }

    @Override
    public int getS32NeAt(final int offset) {
        return inner.getS32NeAt(offset);
    }

    @Override
    public long getS64BeAt(final int offset) {
        return inner.getS64BeAt(offset);
    }

    @Override
    public long getS64LeAt(final int offset) {
        return inner.getS64LeAt(offset);
    }

    @Override
    public long getS64NeAt(final int offset) {
        return inner.getS64NeAt(offset);
    }

    @Override
    public int getU24NeAt(final int offset) {
        return inner.getU24NeAt(offset);
    }

    @Override
    public int getU24BeAt(final int offset) {
        return inner.getU24BeAt(offset);
    }

    @Override
    public int getU24LeAt(final int offset) {
        return inner.getU24LeAt(offset);
    }

    @Override
    public long getU48NeAt(final int offset) {
        return inner.getU48NeAt(offset);
    }

    @Override
    public long getU48BeAt(final int offset) {
        return inner.getU48BeAt(offset);
    }

    @Override
    public long getU48LeAt(final int offset) {
        return inner.getU48LeAt(offset);
    }

    @Override
    public int readableBytes() {
        return inner.readableBytes();
    }

    @Override
    public int readOffset() {
        return inner.readOffset();
    }

    @Override
    public void readOffset(final int readOffset) {
        inner.readOffset(readOffset);
    }

    @Override
    public int readEnd() {
        return inner.readEnd();
    }

    @Override
    public void read(final byte[] destination, final int destinationOffset, final int length) {
        inner.read(destination, destinationOffset, length);
    }

    @Override
    public void read(final BufferWriter destination, final int destinationOffset, final int length) {
        inner.read(destination, destinationOffset, length);
    }

    @Override
    public void read(final ByteBuffer destination) {
        inner.read(destination);
    }

    @Override
    public int read(final WritableByteChannel destination, final int maxLength) {
        return inner.read(destination, maxLength);
    }

    @Override
    public byte readS8() {
        return inner.readS8();
    }

    @Override
    public short readS16Be() {
        return inner.readS16Be();
    }

    @Override
    public short readS16Le() {
        return inner.readS16Le();
    }

    @Override
    public short readS16Ne() {
        return inner.readS16Ne();
    }

    @Override
    public int readS32Be() {
        return inner.readS32Be();
    }

    @Override
    public int readS32Le() {
        return inner.readS32Le();
    }

    @Override
    public int readS32Ne() {
        return inner.readS32Ne();
    }

    @Override
    public long readS64Be() {
        return inner.readS64Be();
    }

    @Override
    public long readS64Ne() {
        return inner.readS64Ne();
    }

    @Override
    public long readS64Le() {
        return inner.readS64Le();
    }

    @Override
    public int readU24Ne() {
        return inner.readU24Ne();
    }

    @Override
    public int readU24Be() {
        return inner.readU24Be();
    }

    @Override
    public int readU24Le() {
        return inner.readU24Le();
    }

    @Override
    public long readU48Ne() {
        return inner.readU48Ne();
    }

    @Override
    public long readU48Be() {
        return inner.readU48Be();
    }

    @Override
    public long readU48Le() {
        return inner.readU48Le();
    }

    @Override
    public void skip(final int length) {
        inner.skip(length);
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
