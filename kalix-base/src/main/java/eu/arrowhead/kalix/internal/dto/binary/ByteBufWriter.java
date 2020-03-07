package eu.arrowhead.kalix.internal.dto.binary;

import eu.arrowhead.kalix.dto.binary.BinaryWriter;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

public class ByteBufWriter implements BinaryWriter {
    private final ByteBuf byteBuf;

    public ByteBufWriter(final ByteBuf byteBuf) {
        this.byteBuf = Objects.requireNonNull(byteBuf, "Expected byteBuf");
    }

    @Override
    public int writeOffset() {
        return byteBuf.writerIndex();
    }

    @Override
    public void writeOffset(final int offset) {
        byteBuf.writerIndex(offset);
    }

    @Override
    public void write(final byte b) {
        byteBuf.writeByte(b);
    }

    @Override
    public void write(final byte[] bytes) {
        byteBuf.writeBytes(bytes);
    }
}
