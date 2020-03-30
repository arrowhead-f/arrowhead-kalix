package se.arkalix.internal.util.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.SchedulerShutdownListener;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Internal
public class DynamicScheduler extends AbstractScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolExecutor.class);

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(0);
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private final Set<SchedulerShutdownListener> shutdownListeners = Collections.synchronizedSet(new HashSet<>(0));

    @Override
    protected ScheduledExecutorService executor() {
        return executor;
    }

    public void shutdown() {
        if (isShuttingDown.getAndSet(true)) {
            throw new IllegalStateException("Already shutting down");
        }
        executor.execute(() -> {
            for (final var listener : shutdownListeners) {
                try {
                    listener.onShutdown(this);
                }
                catch (final Throwable throwable) {
                    logger.error("Unexpected shutdown listener exception caught", throwable);
                }
            }
        });
        executor.shutdown();
    }

    @Override
    public boolean isShuttingDown() {
        return isShuttingDown.get();
    }

    @Override
    public void addShutdownListener(final SchedulerShutdownListener listener) {
        shutdownListeners.add(listener);
    }

    @Override
    public void removeShutdownListener(final SchedulerShutdownListener listener) {
        shutdownListeners.remove(listener);
    }

}
