package se.arkalix.net._internal;

import io.netty.buffer.*;
import se.arkalix.net.BodyIncoming;
import se.arkalix.net.MessageIncoming;
import se.arkalix.util.annotation.Internal;

import java.util.Objects;

@Internal
public abstract class NettyMessageIncoming implements MessageIncoming {
    private final NettyBodyIncoming body;

    protected NettyMessageIncoming(final ByteBufAllocator allocator) {
        body = new NettyBodyIncoming(allocator);
    }

    @Override
    public NettyBodyIncoming body() {
        return body;
    }
}