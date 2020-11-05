package se.arkalix.net._internal;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.DefaultFileRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.encoding.Encoding;
import se.arkalix.encoding.binary._internal.ByteBufWriter;
import se.arkalix.net.BodyOutgoing;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;
import java.util.Optional;

public class NettyBodyOutgoing {
    private static final Logger logger = LoggerFactory.getLogger(BodyOutgoing.class);

    private final Encoding encoding;
    private final long length;
    private final Object content;

    public static NettyBodyOutgoing from(final BodyOutgoing body, final ByteBufAllocator allocator) throws IOException {
        Objects.requireNonNull(allocator, "allocator");

        final Encoding encoding;
        final Object content;
        final long length;

        if (body == null) {
            encoding = null;
            length = 0;
            content = Unpooled.EMPTY_BUFFER;
        }
        else if (body.asEncodable().isPresent()) {
            final var encodable = body.asEncodable().get();
            final var buffer = allocator.buffer();
            final var bufferWriter = new ByteBufWriter(buffer);
            encoding = encodable.encode(bufferWriter);
            length = buffer.readableBytes();
            content = buffer;
        }
        else if (body.asPath().isPresent()) {
            final var path = body.asPath().get();
            final var file = new RandomAccessFile(path.toFile(), "r");
            encoding = null;
            length = file.length();
            content = new DefaultFileRegion(file.getChannel(), 0, length);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Outgoing message body did not contain any " +
                    "supported kind of content; ignoring {}", body);
            }
            encoding = null;
            length = 0;
            content = Unpooled.EMPTY_BUFFER;
        }

        return new NettyBodyOutgoing(encoding, length, content);
    }

    public NettyBodyOutgoing(final Encoding encoding, final long length, final Object content) {
        this.encoding = encoding == Encoding.NONE ? null : encoding;
        this.length = length;
        this.content = Objects.requireNonNull(content, "content");
    }

    public Optional<Encoding> encoding() {
        return Optional.ofNullable(encoding);
    }

    public long length() {
        return length;
    }

    public Object content() {
        return content;
    }
}
