package se.arkalix.net._internal;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.DefaultFileRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.codec.CodecType;
import se.arkalix.io.buf._internal.NettyBuffer;
import se.arkalix.net.BodyOutgoing;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;
import java.util.Optional;

public class NettyBodyOutgoing {
    private static final Logger logger = LoggerFactory.getLogger(BodyOutgoing.class);

    private final CodecType codecType;
    private final long length;
    private final Object content;

    public static NettyBodyOutgoing from(final BodyOutgoing body, final ByteBufAllocator allocator) throws IOException {
        Objects.requireNonNull(allocator, "allocator");

        final CodecType codecType;
        final Object content;
        final long length;

        if (body == null) {
            codecType = null;
            length = 0;
            content = Unpooled.EMPTY_BUFFER;
        }
        else if (body.asEncodable().isPresent()) {
            final var encodable = body.asEncodable().get();
            final var buffer = allocator.buffer();
            final var bufferWriter = new NettyBuffer(buffer);
            codecType = encodable.encode(bufferWriter);
            length = buffer.readableBytes();
            content = buffer;
        }
        else if (body.asPath().isPresent()) {
            final var path = body.asPath().get();
            final var file = new RandomAccessFile(path.toFile(), "r");
            codecType = null;
            length = file.length();
            content = new DefaultFileRegion(file.getChannel(), 0, length);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Outgoing message body did not contain any " +
                    "supported kind of content; ignoring {}", body);
            }
            codecType = null;
            length = 0;
            content = Unpooled.EMPTY_BUFFER;
        }

        return new NettyBodyOutgoing(codecType, length, content);
    }

    public NettyBodyOutgoing(final CodecType codecType, final long length, final Object content) {
        this.codecType = codecType == CodecType.NONE ? null : codecType;
        this.length = length;
        this.content = Objects.requireNonNull(content, "content");
    }

    public Optional<CodecType> codecType() {
        return Optional.ofNullable(codecType);
    }

    public long length() {
        return length;
    }

    public Object content() {
        return content;
    }
}
