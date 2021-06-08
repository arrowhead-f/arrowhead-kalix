package se.arkalix.util.concurrent._internal;

import io.netty.channel.EventLoop;
import se.arkalix.util.annotation.Internal;

import java.util.Optional;

@Internal
public class NettyThread extends Thread {
    private EventLoop eventLoop;

    public NettyThread(final Runnable runnable) {
        super(runnable);
    }

    public static Optional<EventLoop> currentThreadEventLoop() {
        final var thread = Thread.currentThread();
        if (thread instanceof NettyThread) {
            return Optional.ofNullable(((NettyThread) thread).eventLoop());
        }
        return Optional.empty();
    }

    public EventLoop eventLoop() {
        return eventLoop;
    }

    public void eventLoop(final EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }
}
