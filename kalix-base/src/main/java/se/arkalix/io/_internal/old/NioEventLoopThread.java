package se.arkalix.io._internal.old;

import se.arkalix.util.annotation.Internal;
import se.arkalix.util.annotation.ThreadSafe;

import java.util.Objects;
import java.util.Optional;

@Internal
public class NioEventLoopThread extends Thread {
    private final NioEventLoop eventLoop;

    public NioEventLoopThread(final ThreadGroup threadGroup, final NioEventLoop eventLoop, final String name) {
        super(
            Objects.requireNonNull(threadGroup, "threadGroup"),
            Objects.requireNonNull(name, "name")
        );
        this.eventLoop = Objects.requireNonNull(eventLoop, "eventLoop");
    }

    @ThreadSafe
    public static Optional<NioEventLoop> currentThreadEventLoop() {
        final var thread = Thread.currentThread();
        if (thread instanceof NioEventLoopThread) {
            return Optional.of(((NioEventLoopThread) thread).eventLoop());
        }
        return Optional.empty();
    }

    @ThreadSafe
    public NioEventLoop eventLoop() {
        return eventLoop;
    }

    @Override
    public void run() {
        eventLoop.run();
    }
}
