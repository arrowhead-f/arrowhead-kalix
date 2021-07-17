package se.arkalix.io._internal;

import se.arkalix.util.annotation.Internal;
import se.arkalix.util.annotation.ThreadSafe;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Internal
public class NioEventLoopGroup {
    private static final int STATE_INITIAL = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_EXITING = 2;

    private static final NioEventLoopGroup main = new NioEventLoopGroup();

    private final AtomicInteger state;
    private final ThreadGroup threadGroup;
    private final NioEventLoop[] eventLoops;
    private final NioEventLoopThread[] eventLoopThreads;

    private int nextEventLoopIndex = 0;

    @ThreadSafe
    public static NioEventLoopGroup main() {
        return main;
    }

    private NioEventLoopGroup() {
        state = new AtomicInteger(STATE_INITIAL);
        threadGroup = new ThreadGroup("kalix-nio") {
            @Override
            public void uncaughtException(final Thread thread, final Throwable throwable) {
                if (!(throwable instanceof ThreadDeath)) {
                    System.err.print("Thread \"" + thread.getName() + "\" terminated due to ");
                    throwable.printStackTrace(System.err); // TODO: Log exception.
                }
            }
        };
        threadGroup.setDaemon(false);
        eventLoops = new NioEventLoop[Runtime.getRuntime().availableProcessors()];
        eventLoopThreads = new NioEventLoopThread[eventLoops.length];

        for (int i = 0; i < eventLoops.length; ++i) {
            final var eventLoop = new NioEventLoop(this);
            eventLoops[i] = eventLoop;

            final var thread = new NioEventLoopThread(threadGroup, eventLoop, "kalix-nio/" + i);
            thread.setDaemon(false);
            thread.start();
            eventLoopThreads[i] = thread;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "kalix-nio-shutdown"));

        state.set(STATE_RUNNING);
    }

    @ThreadSafe
    public synchronized NioEventLoop nextEventLoop() {

        final var eventLoop = eventLoops[nextEventLoopIndex];
        nextEventLoopIndex += 1;
        if (nextEventLoopIndex >= eventLoops.length) {
            nextEventLoopIndex = 0;
        }
        return eventLoop;
    }

    @ThreadSafe
    public void shutdown() {
        shutdown(Duration.ofMillis(25)); // TODO: Make configurable?
    }

    @ThreadSafe
    public synchronized void shutdown(final Duration timeout) {
        if (!state.compareAndSet(STATE_RUNNING, STATE_EXITING)) {
            return;
        }

        for (final var eventLoop : eventLoops) {
            eventLoop.shutdown();
        }

        long timeRemainingInMs;
        try {
            timeRemainingInMs = timeout.toMillis();
        }
        catch (final ArithmeticException __) {
            timeRemainingInMs = Long.MAX_VALUE;
        }

        for (final var thread : eventLoopThreads) {
            if (timeRemainingInMs > 0) {
                final var before = System.currentTimeMillis();
                try {
                    thread.join(timeRemainingInMs);
                }
                catch (final InterruptedException ignored) {
                }
                timeRemainingInMs -= System.currentTimeMillis() - before;
            }
            else {
                try {
                    threadGroup.interrupt();
                    break;
                }
                catch (final SecurityException exception) {
                    exception.printStackTrace(System.err); // TODO: Log exception.
                }
            }
        }

        threadGroup.destroy();
    }
}
