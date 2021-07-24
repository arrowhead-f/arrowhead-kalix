package se.arkalix.io.event;

import se.arkalix.concurrent.Scheduler;
import se.arkalix.util.annotation.ThreadSafe;

public abstract class EventLoopThread extends Thread implements Scheduler {
    /**
     * Returns a reference to the currently executing thread object, if it is an
     * instance of {@link EventLoopThread}.
     *
     * @return The currently executing thread.
     * @throws IllegalStateException If the thread calling this method is not an
     *                               {@link EventLoopThread}.
     */
    public static EventLoopThread currentThread() {
        final var currentThread = Thread.currentThread();
        if (currentThread instanceof EventLoopThread eventLoopThread) {
            return eventLoopThread;
        }
        throw new IllegalStateException("'" + currentThread.getName() + "' not an event loop thread");
    }

    @ThreadSafe
    public abstract EventLoop eventLoop();
}
