package se.arkalix.internal.net;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.DefaultFileRegion;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.dto.DtoWritable;
import se.arkalix.dto.DtoWriteException;
import se.arkalix.internal.dto.binary.ByteBufWriter;
import se.arkalix.net.MessageEncodingUnspecified;
import se.arkalix.net.MessageEncodingUnsupported;
import se.arkalix.net.MessageOutgoing;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class NettyBodyOutgoing {
    private final Object content;
    private final long length;

    @SuppressWarnings("unchecked")
    public static NettyBodyOutgoing from(
        final MessageOutgoing<?> message,
        final ByteBufAllocator alloc,
        final EncodingDescriptor defaultEncoding
    ) throws DtoWriteException, IOException {

        final var body = message.body().orElse(null);

        final long length;
        final Object content;

        if (body == null) {
            length = 0;
            content = Unpooled.EMPTY_BUFFER;
        }
        else if (body instanceof byte[]) {
            length = ((byte[]) body).length;
            content = Unpooled.wrappedBuffer((byte[]) body);
        }
        else if (body instanceof DtoWritable || body instanceof List) {
            final var encoding = message.encoding().orElse(defaultEncoding);
            if (encoding == null) {
                throw new MessageEncodingUnspecified(message);
            }
            final var dtoEncoding = encoding.asDto()
                .orElseThrow(() -> new MessageEncodingUnsupported(message, encoding));

            final var buffer = alloc.buffer();
            final var bufferWriter = new ByteBufWriter(buffer);
            final var writer = dtoEncoding.writer();
            if (body instanceof DtoWritable) {
                writer.writeOne((DtoWritable) body, bufferWriter);
            }
            else {
                writer.writeMany((List<DtoWritable>) body, bufferWriter);
            }
            length = buffer.readableBytes();
            content = buffer;
        }
        else if (body instanceof Path) {
            final var path = (Path) body;
            final var file = new RandomAccessFile(path.toFile(), "r");
            length = file.length();
            content = new DefaultFileRegion(file.getChannel(), 0, length);
        }
        else if (body instanceof String) {
            final var string = ((String) body);
            final var charset = message.charset().orElse(StandardCharsets.UTF_8);

            final var buffer = alloc.buffer(string.length());
            buffer.writeCharSequence(string, charset);

            length = buffer.readableBytes();
            content = buffer;
        }
        else {
            throw new InternalError("Invalid outgoing body: " + body);
        }

        return new NettyBodyOutgoing(content, length);
    }

    public NettyBodyOutgoing(final Object content, final long length) {
        this.content = Objects.requireNonNull(content);
        this.length = length;
    }

    public long length() {
        return length;
    }

    public Object content() {
        return content;
    }
}
