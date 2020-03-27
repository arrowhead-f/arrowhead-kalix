package se.arkalix.internal.util.concurrent;

import se.arkalix.util.annotation.Internal;

@FunctionalInterface
@Internal
public interface NettySchedulerShutdownListener {
    void onShutdown(final NettyScheduler scheduler) throws Exception;
}
