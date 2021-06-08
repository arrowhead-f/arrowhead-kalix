package se.arkalix.io.buf._internal;

import se.arkalix.io.buf.Buffer;
import se.arkalix.io.buf.BufferReader;
import se.arkalix.io.buf.BufferWriter;
import se.arkalix.util.annotation.Internal;

import java.nio.ByteBuffer;

@Internal
public class EmptyBuffer extends CheckedBuffer {
    private static final EmptyBuffer instance = new EmptyBuffer();

    public static EmptyBuffer instance() {
        return instance;
    }

    private EmptyBuffer() {}

    @Override
    public int writeEnd() {
        return 0;
    }

    @Override
    public void writeEndUnchecked(final int writeEnd) {
        // Does nothing.
    }

    @Override
    protected void writeOffsetUnchecked(final int writeOffset) {
        // Does nothing.
    }

    @Override
    public int writeEndMax() {
        return 0;
    }

    @Override
    public int writeOffset() {
        return 0;
    }

    @Override
    protected Buffer copyUnchecked(final int offset, final int length) {
        return this;
    }

    @Override
    protected Buffer dupeUnchecked() {
        return this;
    }

    @Override
    protected void offsetsUnchecked(final int readOffset, final int writeOffset) {
        // Does nothing.
    }

    @Override
    protected void getAtUnchecked(
        final int offset,
        final byte[] destination,
        final int destinationOffset,
        final int length
    ) {
        throw new IllegalStateException();
    }

    @Override
    protected void getAtUnchecked(
        final int offset,
        final BufferWriter destination,
        final int destinationOffset,
        final int length
    ) {
        throw new IllegalStateException();
    }

    @Override
    protected void getAtUnchecked(final int offset, final ByteBuffer destination) {
        throw new IllegalStateException();
    }

    @Override
    protected byte getS8AtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected short getS16BeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected short getS16LeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected short getS16NeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected int getS32BeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected int getS32LeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected int getS32NeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected long getS64BeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected long getS64LeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected long getS64NeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected int getU24BeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected int getU24LeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected int getU24NeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected long getU48BeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected long getU48LeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected long getU48NeAtUnchecked(final int offset) {
        throw new IllegalStateException();
    }

    @Override
    protected void readUnchecked(final byte[] destination, final int destinationOffset, final int length) {
        // Does nothing.
    }

    @Override
    protected void readUnchecked(final BufferWriter destination, final int destinationOffset, final int length) {
        // Does nothing.
    }

    @Override
    protected void readUnchecked(final ByteBuffer destination) {
        // Does nothing.
    }

    @Override
    protected byte readS8Unchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected short readS16BeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected short readS16LeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected short readS16NeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected int readS32BeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected int readS32LeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected int readS32NeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected long readS64BeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected long readS64LeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected long readS64NeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected int readU24BeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected int readU24LeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected int readU24NeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected long readU48BeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected long readU48LeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected long readU48NeUnchecked() {
        throw new IllegalStateException();
    }

    @Override
    protected void skipUnchecked(final int bytesToSkip) {
        // Does nothing.
    }

    @Override
    protected void setAtUnchecked(final int offset, final byte[] source, final int sourceOffset, final int length) {
        throw new IllegalStateException();
    }

    @Override
    protected void setAtUnchecked(
        final int offset,
        final BufferReader source,
        final int sourceOffset,
        final int length
    ) {
        throw new IllegalStateException();
    }

    @Override
    protected void setAtUnchecked(final int offset, final ByteBuffer source) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS8AtUnchecked(final int offset, final byte value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS16BeAtUnchecked(final int offset, final short value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS16LeAtUnchecked(final int offset, final short value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS16NeAtUnchecked(final int offset, final short value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS24BeAtUnchecked(final int offset, final int value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS24LeAtUnchecked(final int offset, final int value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS24NeAtUnchecked(final int offset, final int value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS32BeAtUnchecked(final int offset, final int value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS32LeAtUnchecked(final int offset, final int value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS32NeAtUnchecked(final int offset, final int value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS48BeAtUnchecked(final int offset, final long value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS48LeAtUnchecked(final int offset, final long value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS48NeAtUnchecked(final int offset, final long value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS64BeAtUnchecked(final int offset, final long value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS64LeAtUnchecked(final int offset, final long value) {
        throw new IllegalStateException();
    }

    @Override
    protected void setS64NeAtUnchecked(final int offset, final long value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeUnchecked(final byte[] source, final int sourceOffset, final int length) {
        // Does nothing.
    }

    @Override
    protected void writeUnchecked(final BufferReader source, final int sourceOffset, final int length) {
        // Does nothing.
    }

    @Override
    protected void writeUnchecked(final ByteBuffer source) {
        // Does nothing.
    }

    @Override
    protected void writeS8Unchecked(final byte value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS16BeUnchecked(final short value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS16LeUnchecked(final short value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS16NeUnchecked(final short value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS24BeUnchecked(final int value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS24LeUnchecked(final int value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS24NeUnchecked(final int value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS32BeUnchecked(final int value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS32LeUnchecked(final int value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS32NeUnchecked(final int value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS48BeUnchecked(final long value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS48LeUnchecked(final long value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS48NeUnchecked(final long value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS64BeUnchecked(final long value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS64LeUnchecked(final long value) {
        throw new IllegalStateException();
    }

    @Override
    protected void writeS64NeUnchecked(final long value) {
        throw new IllegalStateException();
    }

    @Override
    public ByteBuffer asByteBuffer() {
        throw new IllegalStateException();
    }

    @Override
    public void close() {
        // Does nothing.
    }

    @Override
    public int readOffset() {
        return 0;
    }

    @Override
    protected void onClose() {
        throw new IllegalStateException();
    }

    @Override
    protected void readOffsetUnchecked(final int readOffset) {
        // Does nothing.
    }
}
