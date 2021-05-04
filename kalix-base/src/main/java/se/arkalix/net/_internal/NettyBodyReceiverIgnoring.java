package se.arkalix.net._internal;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.util.annotation.Internal;

@Internal
public class NettyBodyReceiverIgnoring implements NettyBodyReceiver {
    private static final NettyBodyReceiverIgnoring instance = new NettyBodyReceiverIgnoring();
    private static final Logger logger = LoggerFactory.getLogger(NettyBodyIncoming.class);

    public static NettyBodyReceiverIgnoring instance() {
        return instance;
    }

    private NettyBodyReceiverIgnoring() {}

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void abort(final Throwable cause) {
        if (logger.isErrorEnabled()) {
            logger.error("incoming message body aborted with error", cause);
        }
    }

    @Override
    public void write(final ByteBuf byteBuf) {
        if (logger.isTraceEnabled()) {
            logger.trace("no body receiver set; incoming {} bytes ignored", byteBuf.readableBytes());
        }
    }

    @Override
    public void close() {
        // Does nothing.
    }
}
