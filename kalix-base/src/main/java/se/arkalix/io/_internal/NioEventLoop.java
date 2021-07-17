package se.arkalix.io._internal;

import se.arkalix.io.IoException;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.annotation.ThreadSafe;
import se.arkalix.util.annotation.Unsafe;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Internal
public class NioEventLoop {
    private static final int STATE_INITIAL = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_EXITING = 2;

    private final NioEventLoopGroup group;
    private final AtomicBoolean isBlockingForEvents;
    private final Selector selector;
    private final AtomicInteger state;
    private final PriorityBlockingQueue<NioEventLoopTask> taskQueue;

    public NioEventLoop(final NioEventLoopGroup group) {
        try {
            this.group = Objects.requireNonNull(group, "group");
            isBlockingForEvents = new AtomicBoolean(false);
            selector = Selector.open();
            state = new AtomicInteger(STATE_INITIAL);
            taskQueue = new PriorityBlockingQueue<>();
        }
        catch (final IOException exception) {
            throw new IoException(exception);
        }
    }

    @ThreadSafe
    public NioEventLoopGroup group() {
        if (isShuttingDown()) {
            throw new IllegalStateException(); // TODO: Better exception.
        }
        return group;
    }

    @ThreadSafe
    public void enqueue(final NioEventLoopTask task) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        if (isShuttingDown()) {
            throw new IllegalStateException(); // TODO: Better exception.
        }
        taskQueue.add(task);
        wakeupIfBlockingForEvents();
    }

    @ThreadSafe
    public void enqueue(final NioEventLoopTask... tasks) {
        if (tasks == null) {
            throw new NullPointerException("tasks");
        }
        enqueue(List.of(tasks));
    }

    @ThreadSafe
    public void enqueue(final Collection<? extends NioEventLoopTask> tasks) {
        if (tasks == null) {
            throw new NullPointerException("tasks");
        }
        if (isShuttingDown()) {
            throw new IllegalStateException(); // TODO: Better exception.
        }
        taskQueue.addAll(tasks);
        wakeupIfBlockingForEvents();
    }

    private void wakeupIfBlockingForEvents() {
        if (isBlockingForEvents.get()) {
            selector.wakeup();
        }
    }

    @ThreadSafe
    public SelectionKey register(final SelectableChannel channel, int ops, Handler handler) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        if (handler == null) {
            throw new NullPointerException("handler");
        }
        if (isShuttingDown()) {
            throw new IllegalStateException(); // TODO: Better exception.
        }
        try {
            return channel.register(selector, ops, handler);
        }
        catch (final ClosedChannelException exception) {
            throw new IoException(exception);
        }
    }

    @Unsafe
    public void run() {
        switch (state.compareAndExchange(STATE_INITIAL, STATE_RUNNING)) {
        case STATE_INITIAL:
            break;

        case STATE_RUNNING:
            throw new IllegalStateException("Already running"); // TODO: Better exception.

        case STATE_EXITING:
            throw new IllegalStateException("Shutting down"); // TODO: Better exception.

        default:
            throw new IllegalStateException("Illegal state: " + state.get());
        }

        final var currentThread = Thread.currentThread();

        for (long numberOfHandledEvents = 0; !currentThread.isInterrupted(); ) {
            try {
                if (numberOfHandledEvents == 0) {
                    isBlockingForEvents.set(true);
                    try {
                        selector.select();
                    }
                    finally {
                        isBlockingForEvents.set(false);
                    }
                }
                else {
                    selector.selectNow();
                }

                final var it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    final var selectionKey = it.next();
                    try {
                        ((Handler) selectionKey.attachment()).handle(selectionKey);
                        numberOfHandledEvents += 1;
                    }
                    catch (final Throwable throwable) {
                        throwable.printStackTrace(); // TODO: Handle exception.
                    }
                    finally {
                        it.remove();
                    }
                }

                final long nowMs = Instant.now().toEpochMilli();
                for (NioEventLoopTask task; (task = taskQueue.poll()) != null; ) {
                    try {
                        if (task.baselineEpochMs() > nowMs) {
                            taskQueue.add(task);
                            break;
                        }

                        task.run();
                        numberOfHandledEvents += 1;
                    }
                    catch (final Throwable throwable) {
                        throwable.printStackTrace(); // TODO: Handle exception.
                    }
                }
            }
            catch (final Throwable throwable) {
                throwable.printStackTrace(); // TODO: Handle exception.
            }
        }
    }

    @ThreadSafe
    void shutdown() {
        if (state.getAndSet(STATE_EXITING) == STATE_RUNNING) {
            taskQueue.clear();
        }
    }

    @ThreadSafe
    public boolean isShuttingDown() {
        return state.get() == STATE_EXITING;
    }

    @FunctionalInterface
    public interface Handler {
        void handle(SelectionKey key) throws Throwable;
    }
}
