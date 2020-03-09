package eu.arrowhead.kalix.internal.dto.binary;

import eu.arrowhead.kalix.dto.binary.BinaryReader;
import eu.arrowhead.kalix.util.annotation.Internal;
import io.netty.buffer.ByteBuf;

@Internal
public class ByteBufReader implements BinaryReader {
    private final ByteBuf byteBuf;

    public ByteBufReader(final ByteBuf buf) {
        byteBuf = buf;
    }

    @Override
    public int readOffset() {
        return byteBuf.readerIndex();
    }

    @Override
    public void readOffset(final int offset) {
        byteBuf.readerIndex(offset);
    }

    @Override
    public int readableBytes() {
        return byteBuf.readableBytes();
    }

    @Override
    public byte peekByte() {
        return byteBuf.getByte(byteBuf.readerIndex());
    }

    @Override
    public byte readByte() {
        return byteBuf.readByte();
    }

    @Override
    public void readBytes(final byte[] target) {
        byteBuf.readBytes(target);
    }

    @Override
    public void readBytes(final byte[] target, final int targetOffset, final int length) {
        byteBuf.readBytes(target, targetOffset, length);
    }

    @Override
    public void getBytes(final int offset, final byte[] target) {
        byteBuf.getBytes(offset, target);
    }

    @Override
    public void skipByte() {
            byteBuf.readByte();
    }

    @Override
    public void skipBytes(final int n) {
            byteBuf.skipBytes(n);
    }
}
