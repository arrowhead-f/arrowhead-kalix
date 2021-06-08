package se.arkalix.net._internal;

import io.netty.buffer.ByteBuf;

public interface NettyBodyReceiver {
    boolean isCancelled();

    void abort(final Throwable cause);

    void write(final ByteBuf byteBuf);

    void close();
}
